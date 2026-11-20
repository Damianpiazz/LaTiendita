-- Legacy shape of the categories table, used only by CategoryFeaturedMigrationIntegrationTest.
-- Spring SQL init runs before Hibernate ddl-auto=update, so the existing row must survive
-- the addition of the featured column with a safe false value.
create table categories (
    id bigserial primary key,
    name varchar(100) not null unique,
    description varchar(500),
    version bigint not null
);

insert into categories (name, description, version)
values ('Legacy Category', 'Creada antes de featured', 1);
