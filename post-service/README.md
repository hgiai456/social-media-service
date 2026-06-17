# profile service
Run image neo4J 
docker run --name neo4j --publish=7474:7474 --publish=7687:7687 -e 'NEO4J_AUTH=neo4j/12345678' neo4j:latest

