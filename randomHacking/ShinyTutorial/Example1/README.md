Bioconductor Shiny Example #1
==============================

Simple app comparing the expression of genes between Tumor and Normal tissue.

This is an example Shiny app featuring some basic analysis of Ovarian Cancer gene expression data collected on the Affymetrix U133A platform. We filter the available genes and samples down to a much smaller matrix to make reproducibility simpler for a broader audience. The R code involved in sampling the data is available in this Gist as an R-Markdown file, and the sampled data are available in this Gist as Rds files.

To run the application, install shiny (`install.packages("shiny")`) then run the following command:

    library(shiny)
    runGist(5924147)

The relevant citation for the original data is available below:

>  Benjamin Frederick Ganzfried, Markus Riester, Benjamin Haibe-Kains, Thomas
>  Risch, Svitlana Tyekucheva, Ina Jazic, Xin Victoria Wang, Mahnaz Ahmadifar,
>  Michael Birrer, Giovanni Parmigiani, Curtis Huttenhower, Levi Waldron.
>  curatedOvarianData: Clinically Annotated Data for the Ovarian Cancer
>  Transcriptome, Database 2013: bat013 doi:10.1093/database/bat013 published
>  online April 2, 2013.