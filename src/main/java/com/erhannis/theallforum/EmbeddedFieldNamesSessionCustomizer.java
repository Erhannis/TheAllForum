package com.erhannis.theallforum;

import java.util.Map;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.sessions.Session;

// https://stackoverflow.com/a/36043791/513038
public class EmbeddedFieldNamesSessionCustomizer implements SessionCustomizer {

  @SuppressWarnings("rawtypes")
  @Override
  public void customize(Session session) throws Exception {
    Map<Class, ClassDescriptor> descriptors = session.getDescriptors();
    for (ClassDescriptor classDescriptor : descriptors.values()) {
      for (DatabaseMapping databaseMapping : classDescriptor.getMappings()) {
        if (databaseMapping.isAggregateObjectMapping()) {
          AggregateObjectMapping m = (AggregateObjectMapping) databaseMapping;
          Map<String, DatabaseField> mapping = m.getAggregateToSourceFields();

          ClassDescriptor refDesc = descriptors.get(m.getReferenceClass());
          for (DatabaseMapping refMapping : refDesc.getMappings()) {
            if (refMapping.isDirectToFieldMapping()) {
              DirectToFieldMapping refDirectMapping = (DirectToFieldMapping) refMapping;
              String refFieldName = refDirectMapping.getField().getName();
              if (!mapping.containsKey(refFieldName)) {
                DatabaseField mappedField = refDirectMapping.getField().clone();
                mappedField.setName(m.getAttributeName() + "_" + mappedField.getName());
                mapping.put(refFieldName, mappedField);
              }
            }
          }
        }
      }
    }
  }
}