Title
========================================================

This is an R Markdown document. Markdown is a simple formatting syntax for authoring web pages (click the **Help** toolbar button for more details on using R Markdown).

When you click the **Knit HTML** button a web page will be generated that includes both content as well as the output of any embedded R code chunks within the document. You can embed an R code chunk like this:


```r
summary(cars)
```

```
##      speed           dist    
##  Min.   : 4.0   Min.   :  2  
##  1st Qu.:12.0   1st Qu.: 26  
##  Median :15.0   Median : 36  
##  Mean   :15.4   Mean   : 43  
##  3rd Qu.:19.0   3rd Qu.: 56  
##  Max.   :25.0   Max.   :120
```


And as a table:

```r
print(head(cars))
```

```
##   speed dist
## 1     4    2
## 2     4   10
## 3     7    4
## 4     7   22
## 5     8   16
## 6     9   10
```



And per http://nsaunders.wordpress.com/2012/08/27/custom-css-for-html-generated-using-rstudio/ as an xtable:

```
Loading required package: xtable
```

<!-- html table generated in R 3.0.2 by xtable 1.7-3 package -->
<!-- Mon Apr 14 18:13:09 2014 -->
<TABLE border=1>
<TR> <TH> speed </TH> <TH> dist </TH>  </TR>
  <TR> <TD align="right"> 4.00 </TD> <TD align="right"> 2.00 </TD> </TR>
  <TR> <TD align="right"> 4.00 </TD> <TD align="right"> 10.00 </TD> </TR>
  <TR> <TD align="right"> 7.00 </TD> <TD align="right"> 4.00 </TD> </TR>
  <TR> <TD align="right"> 7.00 </TD> <TD align="right"> 22.00 </TD> </TR>
  <TR> <TD align="right"> 8.00 </TD> <TD align="right"> 16.00 </TD> </TR>
  <TR> <TD align="right"> 9.00 </TD> <TD align="right"> 10.00 </TD> </TR>
   </TABLE>

