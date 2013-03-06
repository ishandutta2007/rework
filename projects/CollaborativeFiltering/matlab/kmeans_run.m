load MovieInfo.mat;
V = mmread('data/netflix_mm_V.mm');
V = V';
V = V(1:3000,:); % normalize each row

%% Set parameters, initial cluster centers.
%seedsIdx = [1792, 1180, 345, 1542, 32, 48, 27, 118, 238, 270];
seedsIdx = [1792, 1180, 345, 48, 118];
K = size(seedsIdx,2); % number of cluster

%% Run kmeans directly on the latent factor matrix V
display(['Run kmeans with K = ', num2str(K)]);
display('Cluster seeds: ');
for i=1:length(seedsIdx)
    display(MName{seedsIdx(i)});
end
cluster_sample = kmeans_fun(V, K, MName, V(seedsIdx,:));


display('Saving cluster info in kemans_results.txt...');
fid = fopen('kmeans_results.txt', 'w');
for i=1:K
    fprintf(fid, 'Cluster %d : seed = %s\n', i, MName{seedsIdx(i)});
    for j = 1:length(cluster_sample{i})
        fprintf(fid, '%s | ', cluster_sample{i}{j});
        if (mod(j,5) == 0)
            fprintf(fid, '\n');
        end
    end
    if (i < K)
        fprintf(fid, '\n');
    end
end
fclose(fid);
display('done');
