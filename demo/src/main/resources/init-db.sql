CREATE TABLE User (
  id   INT,
  name VARCHAR(20)
);
--;

INSERT INTO User VALUES (1, 'user1');
--;
INSERT INTO User VALUES (2, 'user2');
--;
INSERT INTO User VALUES (3, 'user3');
--;
INSERT INTO User VALUES (4, 'invalid');
--;
INSERT INTO User VALUES (4, 'invalid');
--;

CREATE TABLE Permission (
  idUser INT,
  name   VARCHAR(8),
  value  INT
);
--;

INSERT INTO Permission VALUES (1, 'read', 1);
--;
INSERT INTO Permission VALUES (1, 'write', 1);
--;
INSERT INTO Permission VALUES (2, 'read', 1);
--;
INSERT INTO Permission VALUES (3, 'all', 1);
--;

CREATE PROCEDURE sp_save_user(id_ INT, name_ VARCHAR(20))
  MODIFIES SQL DATA
  INSERT INTO User VALUES (id_, name_);
--;

CREATE PROCEDURE sp_get_users()
  READS SQL DATA
  DYNAMIC RESULT SETS 1
  BEGIN ATOMIC

    DECLARE resultUser CURSOR WITH RETURN FOR
      SELECT
        id,
        name
      FROM User;

    OPEN resultUser;
  END;
--;

CREATE PROCEDURE sp_get_users_by_ids(ids_ INT ARRAY)
  READS SQL DATA
  DYNAMIC RESULT SETS 1
  BEGIN ATOMIC

    DECLARE resultUser CURSOR WITH RETURN FOR
      SELECT
        id,
        name
      FROM User
      WHERE id IN(unnest(ids_));

    OPEN resultUser;
  END;
--;

CREATE PROCEDURE sp_get_simple_users()
  READS SQL DATA
  DYNAMIC RESULT SETS 1
  BEGIN ATOMIC

    DECLARE resultUser CURSOR WITH RETURN FOR
      SELECT id
      FROM User;

    OPEN resultUser;
  END;
--;

CREATE PROCEDURE sp_get_user(idUser INT)
  READS SQL DATA
  DYNAMIC RESULT SETS 1
  BEGIN ATOMIC

    DECLARE resultUser CURSOR WITH RETURN FOR
      SELECT
        id,
        name
      FROM User
      WHERE id = idUser;

    OPEN resultUser;
  END;
--;

CREATE PROCEDURE sp_get_user_with_permissions(idUser_ INT)
  READS SQL DATA
  DYNAMIC RESULT SETS 1
  BEGIN ATOMIC

    DECLARE resultUser CURSOR WITH RETURN FOR
      SELECT
        id,
        name
      FROM User
      WHERE id = idUser_;

    DECLARE resultPermission CURSOR WITH RETURN FOR
      SELECT
        name,
        value
      FROM Permission
      WHERE idUser = idUser_;

    OPEN resultUser;
    OPEN resultPermission;
  END;
--;