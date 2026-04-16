pipelineJob('zudio-pipeline') {
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/rameshlinuxadmin/zudio.git')
                    }
                    branch('main')
                }
            }
            scriptPath('jenkinsfile_windows')
        }
    }

    triggers {
        scm('H/2 * * * *')
    }
}
