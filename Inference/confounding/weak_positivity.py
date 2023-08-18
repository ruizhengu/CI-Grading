import json
import os

import networkx as nx
import numpy as np
import pandas as pd


class Confounding:
    def __init__(self):
        self.mct_csv = "/home/ruizhen/Projects/Experiment/com1003_cafe/mct.csv"
        self.method_coverage_directory = "/home/ruizhen/Projects/Experiment/com1003_cafe/MethodCoverage"
        self.path_graph = "/home/ruizhen/Projects/Experiment/com1003_cafe/Cafe.dot"
        self.graph = nx.DiGraph(nx.drawing.nx_agraph.read_dot(self.path_graph))
        self.causal_graph = nx.DiGraph(nx.drawing.nx_agraph.read_dot("causal_graph.dot"))
        self.name_dict = self.get_all_methods()

    def get_all_methods(self):
        methods_dict = dict()
        for node in self.graph.nodes:
            methods_dict[node] = self.graph.nodes[node]["label"]
        return methods_dict

    def preprocessing(self):
        """
        Add method calls in the methods that are directly called by test cases.

        The method calls are appended in the trace files, the actual orders of method calls are ignored.
        """
        for trace in os.listdir(self.method_coverage_directory):
            with open(os.path.join(self.method_coverage_directory, trace), "a+") as f:
                f.seek(0)
                lines = f.readlines()
                method_calls = [m.replace("\n", "") for m in lines]
                for method in method_calls:
                    method_short = ".".join([method.split(".")[-2], method.split(".")[-1]])

                    for descendant in nx.descendants(self.graph, method_short):
                        descendant_qualified_name = self.graph.nodes[descendant]["label"]
                        if descendant_qualified_name not in method_calls:
                            f.write(descendant_qualified_name + "\n")
            f.close()

    def generate_method_coverage_table(self):
        df = pd.read_csv(self.mct_csv, header=None)
        df.columns = ["method"]

        for trace in os.listdir(self.method_coverage_directory):
            data = []
            with open(os.path.join(self.method_coverage_directory, trace)) as f:
                lines = f.readlines()
                method_calls = [m.replace("\n", "") for m in lines]
                for method in df["method"]:
                    if method in method_calls:
                        data.append(1)
                    else:
                        data.append(0)
                df[trace] = data
        df.to_csv("mct.csv", encoding="utf-8", index=False)

        table = df.drop(df.columns[0], axis=1).to_numpy()
        # return table, [".".join([method.split(".")[-2], method.split(".")[-1]]) for method in df["method"].tolist()]
        return table, [method for method in df["method"].tolist()]

    def weak_positivity_validation(self, mct, methods):
        m = mct[:, 0].size
        n = mct[0, :].size
        weak_positivity_table = np.zeros((m, m))
        for i in range(m):
            for j in range(i + 1, m):
                flag = False
                p11 = False
                p10 = False
                p01 = False
                p00 = False
                for k in range(n):
                    if mct[i][k] == 1 and mct[j][k] == 1:
                        p11 = True
                    if mct[i, k] == 1 and mct[j, k] == 0:
                        p10 = True
                    if mct[i, k] == 0 and mct[j, k] == 1:
                        p01 = True
                    if mct[i, k] == 0 and mct[j, k] == 0:
                        p00 = True
                    if p11 and p10 and p01 and p00:
                        flag = True
                        break
                weak_positivity_table[i, j] = flag
                weak_positivity_table[j, i] = flag
        df = pd.DataFrame(weak_positivity_table, index=methods, columns=methods)
        return df

    def confounder_selection(self, wpt):
        m = wpt.shape[0]
        confounder_set_list = dict()
        for i in range(m):
            i_non_index = wpt.index[i]
            visited = [False for _ in range(m)]
            visited[i] = True
            confounder_set = set()
            initial_set = nx.ancestors(self.causal_graph, self.get_simple_name(wpt.index[i]))
            stack = list(initial_set)
            while len(stack) != 0:
                j = stack.pop()
                j_index = wpt.index.tolist().index(self.name_dict[j])
                if not visited[j_index]:
                    visited[j_index] = True
                    if wpt[i_non_index][self.name_dict[j]]:
                        confounder_set.add(self.name_dict[j])
                    else:
                        stack + list(nx.ancestors(self.causal_graph, j))
            confounder_set_list[i_non_index] = list(confounder_set)
        print(confounder_set_list)
        with open("confounders.json", "w") as f:
            json.dump(confounder_set_list, f)

    @staticmethod
    def get_simple_name(qualified_name):
        return ".".join([qualified_name.split(".")[-2], qualified_name.split(".")[-1]])


if __name__ == '__main__':
    conf = Confounding()
    mct, methods = conf.generate_method_coverage_table()
    weak_positivity_table = conf.weak_positivity_validation(mct, methods)
    conf.confounder_selection(weak_positivity_table)
