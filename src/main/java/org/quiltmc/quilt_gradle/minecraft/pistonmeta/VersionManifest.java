package org.quiltmc.quilt_gradle.minecraft.pistonmeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.quiltmc.quilt_gradle.util.MoreCodecs;

public final class VersionManifest {
	public static final URI URL = URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");

	public static final Codec<VersionManifest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LatestVersions.CODEC.fieldOf("latest").forGetter(VersionManifest::latest),
			Version.CODEC.listOf().fieldOf("versions").forGetter(VersionManifest::versions)
	).apply(instance, VersionManifest::new));

	private static final HttpClient client = HttpClient.newBuilder().build();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final Supplier<VersionManifest> cachedFetched = Suppliers.memoize(VersionManifest::fetch);

	private final LatestVersions latest;
	private final List<Version> versions;
	private final Map<String, Version> versionMap;


	public VersionManifest(LatestVersions latest, List<Version> versions) {
		this.latest = latest;
		this.versions = versions;
		this.versionMap = versions.stream().collect(Collectors.toMap(Version::id, Function.identity()));
	}

	public LatestVersions latest() {
		return this.latest;
	}

	public List<Version> versions() {
		return this.versions;
	}

	public Map<String, Version> versionMap() {
		return this.versionMap;
	}

	public void save(Path path) {
		JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, gson.toJson(json));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static VersionManifest fetchCached() {
		return cachedFetched.get();
	}

	public static VersionManifest fetch() {
		HttpRequest request = HttpRequest.newBuilder(URL).GET().build();
		try {
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			JsonElement json = JsonParser.parseString(response.body());
			return CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static VersionManifest ofFile(Path path) {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			JsonElement json = JsonParser.parseReader(reader);
			return CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public record LatestVersions(String release, String snapshot) {
		public static final Codec<LatestVersions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("release").forGetter(LatestVersions::release),
				Codec.STRING.fieldOf("snapshot").forGetter(LatestVersions::snapshot)
		).apply(instance, LatestVersions::new));
	}

	public record Version(String id, VersionType type, URI url, Date time, Date releaseTime, String sha1,
						  int complianceLevel) {
		public static final Codec<Version> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(Version::id),
				VersionType.CODEC.fieldOf("type").forGetter(Version::type),
				MoreCodecs.URI.fieldOf("url").forGetter(Version::url),
				MoreCodecs.ISO_DATE.fieldOf("time").forGetter(Version::time),
				MoreCodecs.ISO_DATE.fieldOf("releaseTime").forGetter(Version::releaseTime),
				Codec.STRING.fieldOf("sha1").forGetter(Version::sha1),
				Codec.INT.fieldOf("complianceLevel").forGetter(Version::complianceLevel)
		).apply(instance, Version::new));

		public FullVersion expand() {
			HttpRequest request = HttpRequest.newBuilder(this.url).GET().build();
			try {
				HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
				JsonElement json = JsonParser.parseString(response.body());
				return FullVersion.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
			} catch (Exception e) {
				throw new RuntimeException("Error expanding version " + this.id, e);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// make sure all versions can be parsed
		Path got = Paths.get("got.txt");
		Properties props = new Properties();
		if (Files.exists(got)) {
			props.load(Files.newBufferedReader(got));
		}
		try {
			VersionManifest manifest = fetch();
			for (Version version : manifest.versions) {
				if (props.containsKey(version.id)) {
					continue;
				}

				try {
					version.expand();
				} catch (Throwable t) {
					System.out.println("failed on " + version.id);
					throw t;
				}
				props.put(version.id, "true");
			}
		} finally {
			props.store(Files.newBufferedWriter(got, StandardOpenOption.CREATE), "");
		}
	}
}
