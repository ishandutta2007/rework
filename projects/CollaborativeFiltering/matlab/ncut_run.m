load MovieInfo.mat;
V = mmread('data/netflix_mm_V.mm');
V = V';
V = V(1:3000,:);
W = V*V';

%% Set parameters, initial cluster centers.
seedsIdx = [1792, 1180, 345, 48, 118];
K = size(seedsIdx,2); % number of cluster

%% Run normalized cut on the sparsified similarity matrix V*V'.
display(['Run ncut with K = ', num2str(K)]);
display('Initial seed: ');
for i=1:length(seedsIdx)
    display(MName{seedsIdx(i)});
end
U = NCut(W,K+1); % we need one more eigenvector because the smallest is always 1.
cluster_sample = kmeans_fun(U, K, MName, U(seedsIdx,:));
%% 
display('Saving cluster info in kemans_results.txt...');
fid = fopen('ncut_results.txt', 'w');
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


