create table `gameimage`(
	`fileName` varchar(40) not null unique,
    `coordinates` varchar(40) not null,
    constraint pk_gameimage_fileName primary key(fileName)
);

create table `membership`(
	`id` char(10) not null unique,
	`pw` varchar(10) not null,
	`nickname` varchar(8) unique not null,
	constraint pk_membership_id primary key(id)
);

create table `gameRoom`(
	`rp` int(4) default 0,
	`roomName` varchar(25) not null,
    `host` varchar(8) not null unique,
    `guest` varchar(8),
    `state` varchar(8) default '대기중',
    `roomRock` varchar(3) not null,
    `roomPw` char(4),
    constraint pk_gameRoom_host primary key(host),
    constraint fk_gameRoom_host foreign key(host) references membership(nickname)
	on update cascade
    on delete cascade
);

create table `rank`(
	`id` varchar(10) not null unique,
    `nickname` varchar(8) not null unique,
	`rp` int(4) not null default 500,
	`victory` int(4) not null default 0,
	`defeat` int(4) not null default 0,
	`totalGame` int(4) not null default 0,
	constraint pk_rank_id primary key(id),
    constraint fk_rank_id foreign key(id)references membership(id)
	on delete cascade,
    constraint fk_rank_nickname foreign key(nickname) references membership(nickname)
	on update cascade
    on delete cascade
);

delete from membership where id = 'test3';
create table `cointbl`(
	`id` varchar(10) not null unique,
    `coin` int(7) not null default 0,
    constraint pk_coin_id primary key(id),
    constraint fk_coin_id foreign key(id)references membership(id)
    on delete cascade
);

create table `rankhistroy`(
	`no` int not null auto_increment,
	`id` varchar(10) not null,
    `nickname` varchar(8) not null,
    `rp` int(4) not null default 100,
    `victory` int(4) not null default 0,
	`defeat` int(4) not null default 0,
	`totalGame` int(4) not null default 0,
    `sysdate` char(19) not null,
    constraint pk_gamehistroy_no primary key(no)
);

delimiter //
CREATE TRIGGER rank_history_update
AFTER UPDATE
ON rank
FOR EACH ROW
BEGIN
    INSERT INTO rankhistroy
    (
      NO, ID, NICKNAME, RP, VICTORY, DEFEAT, TOTALGAME, SYSDATE
    )
    VALUES
    (
      NULL, NEW.ID , NEW.NICKNAME, NEW.RP,
      NEW.VICTORY, NEW.DEFEAT, NEW.TOTALGAME, NOW()
    );
END//
delimiter ;

delimiter //
CREATE TRIGGER rank_history_delete
AFTER DELETE
ON membership
FOR EACH ROW
BEGIN
    DELETE FROM rankhistroy
    WHERE id = old.id;
END//
delimiter ;
