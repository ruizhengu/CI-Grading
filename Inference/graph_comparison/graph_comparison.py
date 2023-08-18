import networkx as nx
from networkx.drawing.nx_pydot import write_dot

path_reference = "graphs/Cafe_ref.dot"
path_student = "graphs/Cafe_001.dot"

graph_reference = nx.DiGraph(nx.drawing.nx_agraph.read_dot(path_reference))
graph_student = nx.DiGraph(nx.drawing.nx_agraph.read_dot(path_student))

# what nodes are missing
print(graph_reference.nodes() - graph_student.nodes())
# what edges( {('node 1', 'node 2')} ) are missing
print(graph_reference.edges() - graph_student.edges())

tasks = set()

for node in graph_reference.nodes:
    class_name = node.split(".")[0]
    tasks.add(class_name)

for task in tasks:
    graph_reference.add_node(task, label=task)

outcome = "Y"
graph_reference.add_node(outcome, label=outcome)

for node in graph_reference:
    for task in tasks:
        if node.startswith(task) and node != task:
            graph_reference.add_edge(node, task)
    if node != outcome:
        graph_reference.add_edge(node, outcome)

# each method node should also have attributes such as a value (e.g. the number of passed test cases)

write_dot(graph_reference, "graphs/Cafe_ref_with_grade.dot")
