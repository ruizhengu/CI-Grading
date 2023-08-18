import numpy as np

# mct = np.random.randint(0, 2, (10, 5))

mct = np.array(
[[1, 1, 0, 0, 0],
 [1, 1, 0, 0, 0],
 [1, 0, 0, 0, 1],
 [1, 1, 0, 1, 0],
 [0, 1, 0, 1, 0],
 [0, 1, 1, 1, 0],
 [1, 1, 0, 1, 1],
 [0, 1, 0, 1, 1],
 [0, 1, 1, 1, 0],
 [0, 0, 0, 0, 0]]
)

print(mct)

def weak_positivity_validation(mct):
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
    print(weak_positivity_table)

weak_positivity_validation(mct)