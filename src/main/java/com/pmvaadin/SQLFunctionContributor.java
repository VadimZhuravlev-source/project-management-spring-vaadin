package com.pmvaadin;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class SQLFunctionContributor implements MetadataBuilderContributor {

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {

        metadataBuilder.applySqlFunction("get_all_dependencies",
                new StandardSQLFunction(
                        "get_all_dependencies",
                        StandardBasicTypes.STRING
                )
        );

    }

}
