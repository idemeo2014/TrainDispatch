library("ggplot2", lib.loc="/usr/local/lib/R/3.2/site-library")

results <- read.csv("~/Developer/Java/CSE2010AnDS/TrainDispatch/09-35-20-24Apr2016-results.csv")

imp_diff <- data.frame(diff=(results$imp_cost - results$min_cost))
base_diff <- data.frame(diff=(results$base_cost - results$min_cost))

# Histograms
hist(imp_diff$diff, breaks=seq(from=0, to=1.1*max(imp_diff$diff), by=1e6))
hist(base_diff$diff, breaks=seq(from=0, to=1.1*max(base_diff$diff), by=1e6))

# add name coloumn
imp_diff$strategy <- 'improved'
base_diff$strategy <- 'baseline'

cost_diffs <- rbind(imp_diff, base_diff)

# PDF estimation w/ kernel density estimation
ggplot(cost_diffs, aes(diff, fill=strategy)) + geom_density(alpha = 0.2)
