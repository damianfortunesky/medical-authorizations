USE master;
GO

IF DB_ID('db_medical_authorizations') IS NULL
BEGIN
    CREATE DATABASE db_medical_authorizations;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.server_principals
    WHERE name = 'api_user_medauth'
)
BEGIN
    CREATE LOGIN api_user_medauth
    WITH PASSWORD = 'ApiUserMedAuth123!';
END
GO

USE db_medical_authorizations;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals
    WHERE name = 'api_user_medauth'
)
BEGIN
    CREATE USER api_user_medauth FOR LOGIN api_user_medauth;
END
GO

ALTER ROLE db_datareader ADD MEMBER api_user_medauth;
GO

ALTER ROLE db_datawriter ADD MEMBER api_user_medauth;
GO

ALTER ROLE db_ddladmin ADD MEMBER api_user_medauth;
GO