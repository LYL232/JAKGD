from py2neo import Graph
from py2neo.cypher import Record
import argparse
import json
import tqdm

parser = argparse.ArgumentParser()
parser.add_argument('-n', '--nodes_file', type=str, default='./nodes.json')
parser.add_argument(
    '-r', '--relationships_file', type=str, default='./relationships.json')
parser.add_argument('-b', '--batch_size', type=int, default=10000)

args = parser.parse_args()

graph = Graph('http://localhost:7474', auth=('neo4j', '123456'))

relationships_file = args.relationships_file
nodes_file = args.nodes_file
batch_size = args.batch_size

print('dumping node data')
batch = 0
with open(nodes_file, 'w', encoding='utf8') as file:
    while True:
        print(f'fetching node data batch:{batch}, batch_size:{batch_size}')
        node_rerecords = graph.run(
            f'match (n) with distinct n skip '
            f'{batch * batch_size} limit {batch_size} '
            f' return'
            f' labels(n) as l,'
            f' properties(n) as p,'
            f' id(n) as i')
        records = []
        print(f'dumping node data batch:{batch}, batch_size:{batch_size}')
        for each in tqdm.tqdm(node_rerecords):
            each: Record
            labels = each['l']
            identity = each['i']
            properties = each['p']
            records.append(json.dumps({
                'id': identity,
                'properties': properties,
                'labels': labels,
            }, ensure_ascii=False) + '\n')
        if len(records) > 0:
            file.writelines(records)
            if len(records) < batch_size:
                break
        else:
            break
        batch += 1

print('dumping relationship data')
with open(relationships_file, 'w', encoding='utf8') as file:
    batch = 0
    while True:
        print(f'fetching relationship data'
              f' batch:{batch}, batch_size:{batch_size}')
        rel_rerecords = graph.run(
            f'match ()-[r]->() with distinct r '
            f' skip {batch * batch_size} limit {batch_size} '
            f' return'
            f' id(r) as i,'
            f' type(r) as t,'
            f' id(startNode(r)) as s,'
            f' id(endNode(r)) as e,'
            f' properties(r) as p'
        )
        records = []
        print(f'dumping relationship data batch:{batch},'
              f' batch_size:{batch_size}')
        for each in tqdm.tqdm(rel_rerecords):
            each: Record
            start_node = each['s']
            end_node = each['e']
            rel_type = each['t']
            properties = each['p']
            identity = each['i']
            records.append(json.dumps({
                'id': identity,
                'properties': properties,
                'type': rel_type,
                'startNode': start_node,
                'endNode': end_node
            }, ensure_ascii=False) + '\n')
        if len(records) > 0:
            file.writelines(records)
            if len(records) < batch_size:
                break
        else:
            break
        batch += 1
