#!/bin/sh

export DB_HOME="/Users/sriramkrishnan/Projects/postgresql-7.4.6"

export DB_ROOT_USER="apbs_user"
echo "Using root user:" $DB_ROOT_USER
echo ""

export DB_WS_USER="ws_user"
export DB_WS_DB="ws_db"

echo "Creating new user:" $DB_WS_USER
$DB_HOME/bin/createuser -U $DB_ROOT_USER -P $DB_WS_USER
echo ""

echo "Creating new database:" $DB_WS_DB
$DB_HOME/bin/createdb -U $DB_ROOT_USER -O $DB_WS_USER $DB_WS_DB
echo ""

echo "Using user '"$DB_WS_USER"' to create tables inside database:" $DB_WS_DB
$DB_HOME/bin/psql -d $DB_WS_DB -U $DB_WS_USER -f opal_db.sql
