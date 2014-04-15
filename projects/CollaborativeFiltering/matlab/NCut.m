function [ U ] = NCut( W, k )
% Normalize Cut on matrix W, return U containing k eigenvectors of L_sym.
% Equation: See spectralclustering-annotated.pdf slides 44

%% Your code goes in here
% Formulas from page 892 of KM
I = eye(size(W,1));
D = diag(sum(W,2));
Lsym =  I - D^(-1/2) * W * D^(-1/2);
% TODO test bug fixes using matlab when I have access to matlab again
[eigV, eigD] = eigs(Lsym);
U = eigV(:,1:k);

% Per Jay
% The normalized cut essentially solves the following generalized eigenvalue problem

% (D-W)y = \lambda Dy

% By taking the transform: z = D^{1/2}y, the above problem becomes:

% D^{-1/2}(D-W)D^{-1/2} z = \lambda z

% Once you got z back by doing SVD on D^{-1/2}(D-W)D^{-1/2}, you need to recover y by taking y = D^{-1/2}z.

U = D^(-1/2)U

end

% From Jay's solution to this:
% D = diag(sum(W));
% Dnorm = diag(1./sqrt(diag(D)));
% [U, x] = eigs((Dnorm * (D-W) * Dnorm), k, 'SM');
% U = Dnorm * U;
