function [ U ] = NCut( W, k )
% Normalize Cut on matrix W, return U containing k eigenvectors of L_sym.
% Equation: See spectralclustering-annotated.pdf slides 44

%% Your code goes in here
I = eye(size(W,1));
D = diag(sum(W,2));
Lsym =  I - D^-1/2 * W * D^-1/2;
[eigV, eigD] = eig(Lsym);
U = eigV(:,1:k);

end
