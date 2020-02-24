
SET TERM ^ ;
CREATE PROCEDURE PR_REGISTRA_UPDATE_VAL (
    TABELA varchar(100),
    TIPO_ALTERACAO smallint,
    COL_PK_1 varchar(100),
    VAL_PK_1 smallint,
    COL_PK_2 varchar(100) DEFAULT null,
    VAL_PK_2 smallint DEFAULT null,
    COL_PK_3 varchar(100) DEFAULT null,
    VAL_PK_3 smallint DEFAULT null,
    COL_PK_4 varchar(100) DEFAULT null,
    VAL_PK_4 smallint DEFAULT null )
AS
declare variable source_sync varchar(100) default 'localhost:/var/fdb-dbs/sync1.fdb';
declare variable user_sync varchar(20) default 'SYSDBA';
declare variable pass_user_sync varchar(20) default 'masterkey';
BEGIN
  
  
  
    
    if (:col_pk_4 is not null) then 
    begin
        execute statement 'execute procedure PR_REGISTRA_UPDATE_VAL(
            '''||:TABELA||''',
            '||:TIPO_ALTERACAO||',
            '''||:COL_PK_1||''',
            '||:VAL_PK_1||',
            '''||:COL_PK_2||''',
            '||:VAL_PK_2||',
            '''||:COL_PK_3||''',
            '||:VAL_PK_3||',
            '''||:COL_PK_4||''',
            '||:VAL_PK_4||'
        )'
        on EXTERNAL data source :source_sync as USER :user_sync password :pass_user_sync;
        exit;
    end 
  
  
    
    if (:col_pk_3 is not null) then 
    begin
        execute statement 'execute procedure PR_REGISTRA_UPDATE_VAL(
            '''||:TABELA||''',
            '||:TIPO_ALTERACAO||',
            '''||:COL_PK_1||''',
            '||:VAL_PK_1||',
            '''||:COL_PK_2||''',
            '||:VAL_PK_2||',
            '''||:COL_PK_3||''',
            '||:VAL_PK_3||'
        )'
        on EXTERNAL data source :source_sync as USER :user_sync password :pass_user_sync;
        exit;
    end 
  
  
    
    if (:col_pk_2 is not null) then 
    begin
        execute statement 'execute procedure PR_REGISTRA_UPDATE_VAL(
            '''||:TABELA||''',
            '||:TIPO_ALTERACAO||',
            '''||:COL_PK_1||''',
            '||:VAL_PK_1||',
            '''||:COL_PK_2||''',
            '||:VAL_PK_2||'
        )'
        on EXTERNAL data source :source_sync as USER :user_sync password :pass_user_sync;
        exit;
    end 
    
    execute statement 'execute procedure PR_REGISTRA_UPDATE_VAL(
        '''||:TABELA||''',
        '||:TIPO_ALTERACAO||',
        '''||:COL_PK_1||''',
        '||:VAL_PK_1||'
    )'
    on EXTERNAL data source :source_sync as USER :user_sync password :pass_user_sync;
  
END^
SET TERM ; ^
comment on procedure PR_REGISTRA_UPDATE_VAL is 'Realiza a replicação dos dados das tabelas configuradas para que seja possível trabalhar em modo sincronizado';



GRANT EXECUTE ON PROCEDURE PR_REGISTRA_UPDATE_VAL TO  SYNC;

------------------------------------------------------
------------------------------------------------------
--TODA TABELA QUE NECESSITA SER SINCRONIZADA PRECISA:
------------------------------------------------------
------------------------------------------------------


-- Trigger para controlar a geração do ID e a replicação dos seus dados
SET TERM ^ ;
CREATE TRIGGER {TABELA AQUI}_BI FOR {TABELA AQUI} ACTIVE
before insert POSITION 0
AS
BEGIN


    if (CURRENT_USER = 'SYNC') then 
        exit;

    NEW.ID = GEN_ID({GENERATOR DA TABELA AQUI}, 1);
  


END^
SET TERM ; ^



-- Trigger para controlar a replicação dos seus dados
SET TERM ^ ;
CREATE TRIGGER {TABELA AQUI}_AIUD FOR {TABELA AQUI} ACTIVE
after insert or update or delete POSITION 0
as
begin
    
    
    if (CURRENT_USER = 'SYNC') then 
        exit;

    IF (INSERTING) THEN 
        execute procedure PR_REGISTRA_UPDATE_VAL ('{TABELA AQUI}', 0,'ID', new.ID);

    IF (updating) THEN 
        execute procedure PR_REGISTRA_UPDATE_VAL ('{TABELA AQUI}', 1,'ID', new.ID);

    IF (deleting) THEN 
        execute procedure PR_REGISTRA_UPDATE_VAL ('{TABELA AQUI}', 2,'ID', old.ID);

    
end^
SET TERM ; ^


GRANT all ON table {TABELA AQUI} TO  SYNC;
