#!/bin/bash
neo4j-admin dump --database=graph.db --to=/data/
dateTime="`date +%Y-%m-%d-%H-%m-%s`"
mv graph.db.dump "graph.db.${dateTime}.dump"