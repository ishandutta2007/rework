<style type="text/css">
table, td, th {
   border: 1px solid #ccc;
}
</style>


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
<!-- Wed Apr 23 12:43:27 2014 -->
<TABLE border=1>
<TR> <TH> speed </TH> <TH> dist </TH>  </TR>
  <TR> <TD align="right"> 4.00 </TD> <TD align="right"> 2.00 </TD> </TR>
  <TR> <TD align="right"> 4.00 </TD> <TD align="right"> 10.00 </TD> </TR>
  <TR> <TD align="right"> 7.00 </TD> <TD align="right"> 4.00 </TD> </TR>
  <TR> <TD align="right"> 7.00 </TD> <TD align="right"> 22.00 </TD> </TR>
  <TR> <TD align="right"> 8.00 </TD> <TD align="right"> 16.00 </TD> </TR>
  <TR> <TD align="right"> 9.00 </TD> <TD align="right"> 10.00 </TD> </TR>
   </TABLE>


Let's try a wide table
<!-- html table generated in R 3.0.2 by xtable 1.7-3 package -->
<!-- Wed Apr 23 12:43:27 2014 -->
<TABLE border=1>
<TR> <TH> 1 </TH> <TH> 2 </TH> <TH> 3 </TH> <TH> 4 </TH> <TH> 5 </TH> <TH> 6 </TH> <TH> 7 </TH> <TH> 8 </TH> <TH> 9 </TH> <TH> 10 </TH> <TH> 11 </TH> <TH> 12 </TH> <TH> 13 </TH> <TH> 14 </TH> <TH> 15 </TH> <TH> 16 </TH> <TH> 17 </TH> <TH> 18 </TH> <TH> 19 </TH> <TH> 20 </TH> <TH> 21 </TH> <TH> 22 </TH> <TH> 23 </TH> <TH> 24 </TH> <TH> 25 </TH> <TH> 26 </TH> <TH> 27 </TH> <TH> 28 </TH> <TH> 29 </TH> <TH> 30 </TH> <TH> 31 </TH> <TH> 32 </TH> <TH> 33 </TH> <TH> 34 </TH> <TH> 35 </TH> <TH> 36 </TH> <TH> 37 </TH> <TH> 38 </TH> <TH> 39 </TH> <TH> 40 </TH> <TH> 41 </TH> <TH> 42 </TH> <TH> 43 </TH> <TH> 44 </TH> <TH> 45 </TH> <TH> 46 </TH> <TH> 47 </TH> <TH> 48 </TH> <TH> 49 </TH> <TH> 50 </TH>  </TR>
  <TR> <TD align="right"> 4.00 </TD> <TD align="right"> 4.00 </TD> <TD align="right"> 7.00 </TD> <TD align="right"> 7.00 </TD> <TD align="right"> 8.00 </TD> <TD align="right"> 9.00 </TD> <TD align="right"> 10.00 </TD> <TD align="right"> 10.00 </TD> <TD align="right"> 10.00 </TD> <TD align="right"> 11.00 </TD> <TD align="right"> 11.00 </TD> <TD align="right"> 12.00 </TD> <TD align="right"> 12.00 </TD> <TD align="right"> 12.00 </TD> <TD align="right"> 12.00 </TD> <TD align="right"> 13.00 </TD> <TD align="right"> 13.00 </TD> <TD align="right"> 13.00 </TD> <TD align="right"> 13.00 </TD> <TD align="right"> 14.00 </TD> <TD align="right"> 14.00 </TD> <TD align="right"> 14.00 </TD> <TD align="right"> 14.00 </TD> <TD align="right"> 15.00 </TD> <TD align="right"> 15.00 </TD> <TD align="right"> 15.00 </TD> <TD align="right"> 16.00 </TD> <TD align="right"> 16.00 </TD> <TD align="right"> 17.00 </TD> <TD align="right"> 17.00 </TD> <TD align="right"> 17.00 </TD> <TD align="right"> 18.00 </TD> <TD align="right"> 18.00 </TD> <TD align="right"> 18.00 </TD> <TD align="right"> 18.00 </TD> <TD align="right"> 19.00 </TD> <TD align="right"> 19.00 </TD> <TD align="right"> 19.00 </TD> <TD align="right"> 20.00 </TD> <TD align="right"> 20.00 </TD> <TD align="right"> 20.00 </TD> <TD align="right"> 20.00 </TD> <TD align="right"> 20.00 </TD> <TD align="right"> 22.00 </TD> <TD align="right"> 23.00 </TD> <TD align="right"> 24.00 </TD> <TD align="right"> 24.00 </TD> <TD align="right"> 24.00 </TD> <TD align="right"> 24.00 </TD> <TD align="right"> 25.00 </TD> </TR>
  <TR> <TD align="right"> 2.00 </TD> <TD align="right"> 10.00 </TD> <TD align="right"> 4.00 </TD> <TD align="right"> 22.00 </TD> <TD align="right"> 16.00 </TD> <TD align="right"> 10.00 </TD> <TD align="right"> 18.00 </TD> <TD align="right"> 26.00 </TD> <TD align="right"> 34.00 </TD> <TD align="right"> 17.00 </TD> <TD align="right"> 28.00 </TD> <TD align="right"> 14.00 </TD> <TD align="right"> 20.00 </TD> <TD align="right"> 24.00 </TD> <TD align="right"> 28.00 </TD> <TD align="right"> 26.00 </TD> <TD align="right"> 34.00 </TD> <TD align="right"> 34.00 </TD> <TD align="right"> 46.00 </TD> <TD align="right"> 26.00 </TD> <TD align="right"> 36.00 </TD> <TD align="right"> 60.00 </TD> <TD align="right"> 80.00 </TD> <TD align="right"> 20.00 </TD> <TD align="right"> 26.00 </TD> <TD align="right"> 54.00 </TD> <TD align="right"> 32.00 </TD> <TD align="right"> 40.00 </TD> <TD align="right"> 32.00 </TD> <TD align="right"> 40.00 </TD> <TD align="right"> 50.00 </TD> <TD align="right"> 42.00 </TD> <TD align="right"> 56.00 </TD> <TD align="right"> 76.00 </TD> <TD align="right"> 84.00 </TD> <TD align="right"> 36.00 </TD> <TD align="right"> 46.00 </TD> <TD align="right"> 68.00 </TD> <TD align="right"> 32.00 </TD> <TD align="right"> 48.00 </TD> <TD align="right"> 52.00 </TD> <TD align="right"> 56.00 </TD> <TD align="right"> 64.00 </TD> <TD align="right"> 66.00 </TD> <TD align="right"> 54.00 </TD> <TD align="right"> 70.00 </TD> <TD align="right"> 92.00 </TD> <TD align="right"> 93.00 </TD> <TD align="right"> 120.00 </TD> <TD align="right"> 85.00 </TD> </TR>
   </TABLE>

