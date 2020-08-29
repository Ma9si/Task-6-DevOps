job("Job 1") {
	description()
	keepDependencies(false)
	scm {
		git {
			remote {
				github("Ma9si/task-3-k8s", "https")
			}
			branch("*/master")
		}
	}
	disabled(false)
	triggers {
		scm("* * * * *") {
			ignorePostCommitHooks(false)
		}
	}
	concurrentBuild(false)
	steps {
		shell("""if(ls | grep .html)
then
kubectl apply -f html-pod.yml
kubectl cp \$(ls | grep .html) html:/usr/local/apache2/htdocs
fi

if(ls | grep .php)
then
kubectl apply -f php-pod.yml
kubectl cp \$(ls | grep .php) html:/usr/local/apache2/htdocs
fi""")
	}
}

job("Job 2") {
	description()
	keepDependencies(false)
	disabled(false)
	concurrentBuild(false)
	triggers {
	      upstream {
		     upstreamProjects('Job 1')
			threshold('SUCCESS')
			}
		}	
	steps {
		shell("""status=\$(curl -o /dev/null  -s -w "%{http_code}" 192.168.99.100:31001/project.html)
if [[ \$status = 200 ]]
then
exit 0
else
exit 1
fi""")
	}
	publishers {
		mailer("mansisaini848@gmail.com", false, false)
	}
}
buildPipelineView('TASK-6') {
    filterBuildQueue()
    filterExecutors()
    title('Task 6 view')
    displayedBuilds(5)
    selectedJob('Job 1')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(60)
}
