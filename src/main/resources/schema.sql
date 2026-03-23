create table if not exists posts
(
    id            bigserial primary key,
    title         varchar(256) not null,
    text          text not null,
    tags          jsonb,
    likes_count   integer not null default 0,
    comments_count integer not null default 0
    );


insert into posts (title, text, tags, likes_count, comments_count)
select 'Название поста 1', 'Текст поста в формате Markdown...', '["tag_1", "tag_2"]'::jsonb, 5, 1
    where not exists (select 1 from posts limit 1);

insert into posts (title, text, tags, likes_count, comments_count)
select 'Второй пост', 'Текст второго поста...', '["tag_3"]'::jsonb, 2, 0
    where not exists (select 1 from posts where id = 2);