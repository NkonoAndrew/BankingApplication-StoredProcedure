--docker cp ./p2.sql c8e77df0a2ff:./database/config/db2inst1/.
----
--db2 -td"@" -f p2.sql
--
CONNECT TO CS157A@
--
--
DROP PROCEDURE P2.CUST_CRT@
DROP PROCEDURE P2.CUST_LOGIN@
DROP PROCEDURE P2.ACCT_OPN@
DROP PROCEDURE P2.ACCT_CLS@
DROP PROCEDURE P2.ACCT_DEP@
DROP PROCEDURE P2.ACCT_WTH@
DROP PROCEDURE P2.ACCT_TRX@
DROP PROCEDURE P2.ADD_INTEREST@

CREATE OR REPLACE PROCEDURE P2.CUST_CRT
(IN p_name CHAR(15), IN p_gender CHAR(1), IN p_age INTEGER, IN p_pin INTEGER, OUT identification INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
  DECLARE SQLSTATE CHAR(5);
        BEGIN
          INSERT INTO p2.customer(Name, Gender, Age, Pin)
          VALUES(p_name, p_gender, p_age, p2.encrypt(p_pin));
            IF p_gender NOT IN ('F','M') THEN
              SET sql_code = -100;
              SET err_msg = 'Invalid gender';
            ELSEIF p_age <= 0 THEN
              SET sql_code = -100;
              SET err_msg = 'Invalid age';
            ELSEIF p_pin < 0 THEN
              SET sql_code = -100;
              SET err_msg = 'Invalid pin';
            ELSE
              SET err_msg = 'NO ERRORS CREATING NEW CUSTOMER';
              SET sql_code = 000;
               BEGIN
                   declare c1 cursor for
                       SELECT ID FROM p2.customer WHERE name = p_name AND gender = p_gender AND age = p_age;
                   open c1;
                        fetch c1 into identification;
                   close c1;
               END;
            END IF;
        END;
END@


CREATE OR REPLACE PROCEDURE P2.CUST_LOGIN
(IN p_id INTEGER, IN p_pin INTEGER, OUT valid INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
BEGIN
    BEGIN
       DECLARE pin_ INTEGER DEFAULT 0;
       DECLARE id_ INTEGER DEFAULT 0;
       SET pin_ = (SELECT pin FROM p2.customer WHERE id = p_id AND pin = p2.encrypt(p_pin));
       SET id_ = (SELECT id from p2.customer WHERE id = p_id and p2.encrypt(p_pin));

      IF p_pin = p2.decrypt(pin_) AND id_ = p_id THEN
        SET valid = 1;
        SET sql_code = 000;
        SET err_msg = 'Logged In!';
      ELSE
        SET valid = 0;
        SET err_msg = 'Invalid ID or password';
      END IF;
    END;
END@

CREATE OR REPLACE PROCEDURE P2.ACCT_OPN
(IN p_id CHAR(15),  IN p_balance INTEGER, IN p_type CHAR(1), OUT p_number INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
BEGIN
    IF  p_type NOT IN ('C', 'S', 'c', 'C') THEN
        SET err_msg = 'Invalid account type';
        SET sql_code = -100;
    ELSEIF p_balance < 0 THEN
        SET err_msg = 'Invalid balance';
        SET sql_code = -100;
    ELSE
        SET sql_code = 000;
          INSERT INTO p2.account (id, balance, type, status) VALUES (p_id, p_balance, p_type, 'A');
            BEGIN
               declare c1 cursor for
                    SELECT number FROM p2.account WHERE id = p_id;
                         open c1;
                         fetch c1 into p_number;
                    close c1;
            END;
        SET err_msg = '';
    END IF;
END@


CREATE OR REPLACE PROCEDURE P2.ACCT_CLS
(IN p_number CHAR(15), OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
BEGIN
DECLARE SQLSTATE CHAR(5);
    UPDATE p2.account SET status = 'I', balance = 0 WHERE number = p_number;
    SET sql_code = SQLSTATE;
    IF sql_code != 00000 THEN
        SET err_msg = 'ERROR IN CLOSING ACCOUNT';
    ELSE
        SET err_msg = 'ACCOUNT CLOSED';
        SET sql_code = 00000;
    END IF;
END@


CREATE OR REPLACE PROCEDURE P2.ACCT_DEP
(IN p_number CHAR(15), IN amount INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
BEGIN
DECLARE SQLSTATE CHAR(5);
    UPDATE p2.account SET balance = balance + amount WHERE number = p_number AND status = 'A';
    SET sql_code = SQLSTATE;
        IF sql_code != 00000 THEN
            SET err_msg = 'ERROR: CANNOT DEPOSIT TO ACCOUNT';
            SET sql_code = -100;
            ELSE
            SET err_msg = 'AMOUNT DEPOSITED';
            SET sql_code = 00000;
        END IF;
END@

CREATE OR REPLACE PROCEDURE P2.ACCT_WTH
(IN p_number CHAR(15), IN amount INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
BEGIN
DECLARE SQLSTATE CHAR(5);
    IF amount < 0 THEN
        SET err_msg = 'NEGATIVE AMOUNT';
        SET sql_code = -100;
    ELSE
        UPDATE p2.account SET balance = balance - amount WHERE number = p_number;
        SET err_msg = 'SUCCESSFUL WITHDRAW';
        SET sql_code = 0000;
    END IF;
END@


CREATE OR REPLACE PROCEDURE P2.ACCT_TRX
(IN src_account INTEGER, IN dest_acct INTEGER, IN amount INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
BEGIN
DECLARE SQLSTATE CHAR(5);
    CALL P2.ACCT_WTH(src_account, amount, sql_code, err_msg);
    CALL P2.ACCT_DEP(dest_acct, amount, sql_code, err_msg);
        SET sql_code = SQLSTATE;
          IF sql_code != 00000 THEN
                    SET err_msg = 'ERROR: CANNOT TRANSFER';
                    SET sql_code = -100;
                    ELSE
                    SET err_msg = 'SUCCESSFUL TRANSFER';
                    SET sql_code = 00000;
          END IF;

END@

create PROCEDURE P2.ADD_INTEREST
(IN savings_rate float, IN checking_rate float, OUT sql_code varchar(20), OUT err_msg varchar(50))
LANGUAGE SQL
    BEGIN
    DECLARE SQLSTATE CHAR(5);
        update p2.account set balance = (1 + checking_rate) * balance where type = 'C' and status = 'A';
        SET sql_code = SQLSTATE;
                IF sql_code != 00000 THEN
                    SET err_msg = 'NO INTEREST ON INACTIVE ACCT';

                ELSE
                    SET err_msg = 'INTEREST ADDED';
                    SET sql_code = 0000;
                END IF;
        update p2.account set balance = (1 + savings_rate) * balance where type = 'S' and status = 'A';
        SET sql_code = SQLSTATE;
        IF sql_code != 00000 THEN
            SET err_msg = 'NO INTEREST ON INACTIVE ACCT';
        ELSE
            SET err_msg = 'INTEREST ADDED';
            SET sql_code = 0000;
        END IF;
END @

--
--TERMINATE@
--
--
