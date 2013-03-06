library(ggplot2)

setwd('/Users/deflaux/rework/projects/CollaborativeFiltering')
set.seed(42)

# 4.2 (a)
k = c(2,4,6,8,10)
rmse = c(0.8780483482699089,0.8565597271426147,0.8431536979900152,0.8315517218713753,0.8292356981915701)
runtime = c(412.273, 438.217, 512.128, 692.346, 893.649)
# k2
# Engine finished in: 412.273 secs.
# 9:40:06 AM ALS main - INFO:   Train RMSE: 0.8780483482699089, total edges:99072112
# k4
# 9:57:22 AM engine run - INFO:   Engine finished in: 438.217 secs.
# 9:57:22 AM ALS main - INFO:   Train RMSE: 0.8565597271426147, total edges:99072112
# k6 
# 11:16:53 AM engine run - INFO:   Engine finished in: 512.128 secs.
# 11:16:53 AM ALS main - INFO:   Train RMSE: 0.8431536979900152, total edges:99072112
# k8
# 11:46:50 AM engine run - INFO:   Engine finished in: 692.346 secs.
# 11:46:50 AM ALS main - INFO:   Train RMSE: 0.8315517218713753, total edges:99072112
# k10
# 12:54:59 PM engine run - INFO:   Engine finished in: 893.649 secs.
# 12:54:59 PM ALS main - INFO:   Train RMSE: 0.8292356981915701, total edges:99072112

qplot(k, rmse,    
      geom=c("point", "smooth"), alpha=I(1/50),
      main='Training RMSE against k')
ggsave("rmseVersusK.jpg")

qplot(k, runtime,    
      geom=c("point", "smooth"), alpha=I(1/50),
      main='Engine runtime against k')
ggsave("runtimeVersusK.jpg")

