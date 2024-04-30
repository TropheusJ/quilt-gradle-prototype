package org.quiltmc.quilt_gradle.remapping;

import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.attributes.CompatibilityCheckDetails;

public class MappingsAttribute {
	public static final Attribute<String> INSTANCE = Attribute.of("org.quiltmc.quilt_gradle.mappings", String.class);

	public static void init(AttributesSchema schema) {
		schema.attribute(INSTANCE);
		schema.getMatchingStrategy(INSTANCE).getCompatibilityRules().add(RemapAttributeCompat.class);
	}

	public static class RemapAttributeCompat implements AttributeCompatibilityRule<String> {
		@Override
		public void execute(CompatibilityCheckDetails<String> details) {
			System.out.println("checking " + details.getConsumerValue() + " against " + details.getProducerValue());
			details.compatible();
		}
	}
}
