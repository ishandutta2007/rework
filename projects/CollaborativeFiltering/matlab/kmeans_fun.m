function [C] = kmeans_fun( X, K, MName, seeds )
% Perform Kmeans on Movie matrix X, using k clusters. For each cluster,
% ouput the names of 20 movies which are closest to the center.
    C = {};
    opts = statset('MaxIter', 500, 'Display', 'final');
    [idx, centers, sumD, D] = kmeans(X, K, 'Start', seeds, 'Options', opts);    
    for k = 1:K
        movieidx = find(idx==k);
        [Y,I] = sort(D(movieidx,k));
        top20 = movieidx(I(1:min(length(I),20)));
        C{k} = {MName{top20}};
    end
end

