package io.jpm.core.jpm_repository.parse.domain.cache;

public interface EntityRelationRegistry {

   void registerPkFieldType(String entityName, String pkFieldType);
   void registerPkFieldName(String entityName, String pkFieldName);
   void registerFk(String entityName, String fkFieldName, String parentEntityName);
   String resolveFkType(String entityName, String fkFieldName);
   String getPkFieldType(String entityName);
   String getPkFieldName(String entityName);
}
