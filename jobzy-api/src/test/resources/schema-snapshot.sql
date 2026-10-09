
    create table vacancy_description_jpa_entity (
        created_at datetime2(7),
        last_modified_at datetime2(7),
        vacancy_id uniqueidentifier not null,
        about_us varchar(255),
        job_description varchar(255),
        modified_by varchar(255),
        source varchar(255) check ((source in ('MANUAL','GENERATED'))),
        summary varchar(255),
        tasks varchar(255),
        what_we_offer varchar(255),
        primary key (vacancy_id)
    );

    create table vacancy_jpa_entity (
        max_hours numeric(38,2),
        min_hours numeric(38,2),
        created_at datetime2(7),
        last_modified_at datetime2(7),
        id uniqueidentifier not null,
        category varchar(255) check ((category in ('ADMINISTRATION','CONSTRUCTION','CUSTOMER_SUPPORT','DESIGN_CREATIVE','EDUCATION','ENGINEERING','FACILITY_SERVICES','FINANCE','HEALTHCARE','HOSPITALITY','HUMAN_RESOURCES','LEGAL','LOGISTICS_SUPPLY_CHAIN','MANAGEMENT','MARKETING','OPERATIONS','PRODUCTION_MANUFACTURING','RETAIL','SALES','SCIENCE_RESEARCH','OTHER'))),
        city varchar(255),
        country varchar(255),
        job_title varchar(255),
        modified_by varchar(255),
        status varchar(255) check ((status in ('DRAFT','PUBLISHED','FILLED','CLOSED'))),
        workplace_type varchar(255) check ((workplace_type in ('ONSITE','REMOTE','HYBRID'))),
        primary key (id)
    );

    alter table vacancy_description_jpa_entity 
       add constraint FKsovxuqvpmdiac3gj6ji1gn1g 
       foreign key (vacancy_id) 
       references vacancy_jpa_entity;
