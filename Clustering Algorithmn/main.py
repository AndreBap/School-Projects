import random
from scipy.spatial import distance_matrix
import numpy as np
import scipy.io
import matplotlib.pyplot as plt


# assigning data points to the nearest centroid.
# Returns a list of the index of the centroid that the matching point is assigned.
# Takes data points and a list of centroids as D and C respectively
def assign(D, C):
    g = 0
    assigned = []
    dist = distance_matrix(D,C)
    for row in D:
        assigned.append(list(dist[g]).index(min(dist[g])))
        g = g + 1
    return assigned

# Changes centroid positions to the mean of the points assigned to it.
# Takes data points and list of their assignments as D and C respectively.
def centroid(D, C):
    temp = set(C)
    C_set = list(temp)
    new_centroid = [[0, 0]] * len(C_set)
    i = 0
    for rowC in range(0, len(C_set)):
        length = 0
        xsum = 0
        ysum = 0
        j = 0
        for rowD in range(len(C)):
            if C[j] == i:
                xsum = D[j][0] + xsum
                ysum = D[j][1] + ysum
                length = length + 1
            j = j + 1
        new_centroid[i] = [float(xsum) / length, float(ysum) / length]
        i = i + 1
    return new_centroid


#strategy 1 of selecting K intial centroids from the data points
def rand_centroid(D, k):
    centroids = []

    for item in range(0,k):
        pick = random.randint(0,len(D)-1)
        centroids.append(D[pick])
    return centroids

#strategy 2 of selecting first centroid randomly and following ones with highest averge distance to previous.
def avg_dist_centroid(D, k):
    centroids = []
    picked = []
    picked.append(random.randint(0, len(D) - 1))
    centroids.append(D[picked[0]])

    dist_matrix = distance_matrix(D,D)
    for item in range(1, k):
        avg_dist = np.zeros(len(D))
        j = 0
        for cent in picked:
            avg_dist = avg_dist + np.array(dist_matrix[picked[j]])
            j = j + 1
        avg_dist = avg_dist / len(picked)
        pick = np.argmax(avg_dist)

        g = 0
        while pick in set(picked):
            avg_dist[pick] = 0
            pick = np.argmax(avg_dist)

        picked.append(pick)
        centroids.append(D[pick])

    return centroids

# k means function for strategy 1.
# Returns 2-d list. First row is a list of centroids and the second the assignments of each data point.
def k_means_rand(D,k):
    centroids = rand_centroid(D,k)
    assignments = assign(D,centroids)
    changed = 1
    while changed == 1:
        new_centroids = centroid(D,assignments)
        new_assignments = assign(D, new_centroids)
        if assignments == new_assignments:
            changed = 0
        centroids = new_centroids
        assignments = new_assignments
    return [centroids, assignments]


# k means function for strategy 2.
# Returns 2-d list. First row is a list of centroids and the second the assignments of each data point.
def k_means_dist(D,k):
    centroids = avg_dist_centroid(D,k)
    assignments = assign(D,centroids)
    changed = 1
    while changed == 1:
        new_centroids = centroid(D,assignments)
        new_assignments = assign(D, new_centroids)

        if (assignments == new_assignments) and (centroids == new_centroids):
            changed = 0
        centroids = new_centroids
        assignments = new_assignments
    return [centroids, assignments]


def loadfile(filename):
    mat = scipy.io.loadmat(filename)
    return mat['AllSamples']

# takes a data set and the list of matching cluster assignments
# and returns lists of data points for each cluster
def cluster(D,C):
    # find number of unique centroids
    temp = set(C)
    C_set = list(temp)
    clusters = []
    i = 0

    for row in C_set:
        j = 0
        clusters.append([])

        for row2 in C:
            if C[j] == i:
                clusters[i].append(list(D[j]))
            j = j + 1
        i = i + 1
    return clusters


def objective_function(Clusters, Centroids):
    i = 0
    result = 0
    for row in Centroids:
        j = 0
        for row2 in Clusters[i]:
            result = result + (Clusters[i][j][0] - Centroids[i][0])**2 +(Clusters[i][j][1] - Centroids[i][1])**2
            j = j + 1
        i = i + 1
    return result

# function which receives clusters and title for graph and plots them in a scatterplot by color.
def graph_clusters(C, T):
    k = len(C)
    x = []
    y = []
    i = 0
    for row in range(0,k):
        j = 0
        x.append([])
        y.append([])

        for row2 in range(0,len(C[i])):
            x[i].append(C[i][j][0])
            y[i].append(C[i][j][1])
            j = j + 1
        i = i + 1

    colors = ("red","grey","orange","yellow", "green", "blue","purple","violet","brown","black")
    groups = ("1", "2", "3","4","5","6","7","8","9","10")

    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1)

    g = 0
    for data, color, group in zip(C, colors, groups):
        ax.scatter(x[g],y[g], alpha=0.8, c=color, edgecolors='none', s=30, label=group)
        g = g + 1

    plt.title(T)
    plt.legend(loc=2)
    plt.show()

# display graph of objection function values.
# takes list of objective function values and title for graph as input.
def plot_obj_func(O,T):
    plt.plot([2,3,4,5,6,7,8,9,10], O)
    plt.title(T)
    plt.xlabel('K')
    plt.ylabel('Var')
    plt.show()


def run_dist(D, k, Title):
    result = k_means_dist(D, k)
    clusters = cluster(D, result[1])
    obj_func = objective_function(clusters, result[0])
    graph_clusters(clusters, Title)

    return obj_func


def run_rand(D, k, Title):
    result = k_means_rand(D, k)
    clusters = cluster(D, result[1])
    obj_func = objective_function(clusters, result[0])
    graph_clusters(clusters, Title)

    return obj_func





D = loadfile("dcG9m8gvEemRPArJHNevzg_b3ba7dd655bd4dafb06a913ff8515732_AllSamples.mat")

obj_func_strat_1 = []
obj_func_strat_2 = []
obj_func_strat_1_repeat = []
obj_func_strat_2_repeat = []

# run strategy 1 for k = 2-10 and collect results of objective function
obj_func_strat_1.append(run_rand(D,2,'Strategy 1 K = 2 First Run'))
obj_func_strat_1.append(run_rand(D,3,'Strategy 1 K = 3 First Run'))
obj_func_strat_1.append(run_rand(D,4,'Strategy 1 K = 4 First Run'))
obj_func_strat_1.append(run_rand(D,5,'Strategy 1 K = 5 First Run'))
obj_func_strat_1.append(run_rand(D,6,'Strategy 1 K = 6 First Run'))
obj_func_strat_1.append(run_rand(D,7,'Strategy 1 K = 7 First Run'))
obj_func_strat_1.append(run_rand(D,8,'Strategy 1 K = 8 First Run'))
obj_func_strat_1.append(run_rand(D,9,'Strategy 1 K = 9 First Run'))
obj_func_strat_1.append(run_rand(D,10,'Strategy 1 K = 10 First Run'))

# repeat run of strategy 1
obj_func_strat_1_repeat.append(run_rand(D,2,'Strategy 1 K = 2 Second Run'))
obj_func_strat_1_repeat.append(run_rand(D,3,'Strategy 1 K = 3 Second Run'))
obj_func_strat_1_repeat.append(run_rand(D,4,'Strategy 1 K = 4 Second Run'))
obj_func_strat_1_repeat.append(run_rand(D,5,'Strategy 1 K = 5 Second Run'))
obj_func_strat_1_repeat.append(run_rand(D,6,'Strategy 1 K = 6 Second Run'))
obj_func_strat_1_repeat.append(run_rand(D,7,'Strategy 1 K = 7 Second Run'))
obj_func_strat_1_repeat.append(run_rand(D,8,'Strategy 1 K = 8 Second Run'))
obj_func_strat_1_repeat.append(run_rand(D,9,'Strategy 1 K = 9 Second Run'))
obj_func_strat_1_repeat.append(run_rand(D,10,'Strategy 1 K = 10 Second Run'))

# run strategy 2 for k = 2-10 and collect results of objective function
obj_func_strat_2.append(run_dist(D,2,'Strategy 2 K = 2 First Run'))
obj_func_strat_2.append(run_dist(D,3,'Strategy 2 K = 3 First Run'))
obj_func_strat_2.append(run_dist(D,4,'Strategy 2 K = 4 First Run'))
obj_func_strat_2.append(run_dist(D,5,'Strategy 2 K = 5 First Run'))
obj_func_strat_2.append(run_dist(D,6,'Strategy 2 K = 6 First Run'))
obj_func_strat_2.append(run_dist(D,7,'Strategy 2 K = 7 First Run'))
obj_func_strat_2.append(run_dist(D,8,'Strategy 2 K = 8 First Run'))
obj_func_strat_2.append(run_dist(D,9,'Strategy 2 K = 9 First Run'))
obj_func_strat_2.append(run_dist(D,10,'Strategy 2 K = 10 First Run'))

# repeat run of strategy 2
obj_func_strat_2_repeat.append(run_dist(D,2,'Strategy 2 K = 2 Second Run'))
obj_func_strat_2_repeat.append(run_dist(D,3,'Strategy 2 K = 3 Second Run'))
obj_func_strat_2_repeat.append(run_dist(D,4,'Strategy 2 K = 4 Second Run'))
obj_func_strat_2_repeat.append(run_dist(D,5,'Strategy 2 K = 5 Second Run'))
obj_func_strat_2_repeat.append(run_dist(D,6,'Strategy 2 K = 6 Second Run'))
obj_func_strat_2_repeat.append(run_dist(D,7,'Strategy 2 K = 7 Second Run'))
obj_func_strat_2_repeat.append(run_dist(D,8,'Strategy 2 K = 8 Second Run'))
obj_func_strat_2_repeat.append(run_dist(D,9,'Strategy 2 K = 9 Second Run'))
obj_func_strat_2_repeat.append(run_dist(D,10,'Strategy 2 K = 10 Second Run'))

# graphing the objective function
plot_obj_func(obj_func_strat_1, 'Strategy 1 First Iteration')
plot_obj_func(obj_func_strat_1_repeat, 'Strategy 1 Second Iteration')
plot_obj_func(obj_func_strat_2, 'Strategy 2 First Iteration')
plot_obj_func(obj_func_strat_2_repeat, 'Strategy 2 Second Iteration')


