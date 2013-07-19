####Code for BioC workshop on ReportingTools
##July 19 2013 Seattle WA
##shiny and ReportingTools how to

##For more details on the server.R and ui.R files see the 
##shiny tutorial at http://rstudio.github.com/shiny/tutorial/

##Look over shinyReportingTools.Rnw for more details
setwd("/data/ReportingToolsWorkshop")
library(shiny)

myRunApp <- function(...)
{
  try(hostname <- suppressWarnings(system2(c("hostname", "-d"),
                                           stdout=TRUE, stderr=NULL)),
      silent=TRUE)
  if (exists("hostname") && length(hostname) &&
        grepl("ec2\\.internal", hostname))
  {
    dots <- list(...)
    dots[["launch.browser"]] <- FALSE
    if (is.null(dots[['port']])) dots[['port']] <- 8103L
    library(httr)
    public.dns <-
      content(GET("http://169.254.169.254/latest/meta-data/public-hostname"))
    url <- paste0("http://", public.dns, ":", dots[["port"]])
    #cat("Press Reload in the browser window that appears.\n")
    cat("If you don't see a new window, ")
    cat("try disabling your popup blocker and try again.\n")
    cat("Press ESCAPE in this window when done.\n")
    browseURL(url)
    do.call(runApp, dots)
    
  } else {
    runApp(...)
  }
}
##simple shiny example:
dir <- system.file("examples", "01_hello", package="shiny")
myRunApp(dir)

##may need to copy ui.R and server.R to reports dir and run this from within it if there are permission issues
#setwd("reports")
##need myRunApp for to run in RStudio
myRunApp()

##typically runs with this command:
#runApp()

