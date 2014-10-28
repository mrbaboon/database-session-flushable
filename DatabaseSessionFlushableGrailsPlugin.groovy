import grails.plugin.databasesessionflushable.*
import grails.util.Environment
import grails.util.Metadata

import org.springframework.web.filter.DelegatingFilterProxy
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate

import org.apache.commons.dbcp.BasicDataSource

class DatabaseSessionFlushableGrailsPlugin {
	String version = '1.2.5.8.6'
	String grailsVersion = '2.0.0 > *'
	String title = 'Database Session Flushable Plugin'
	String author = 'Robert Fischer'
	String authorEmail = 'robert.fischer@smokejumperit.com'
	def developers = [ [ name: "Burt Beckwith", email: "beckwithb@vmware.com" ]]
	String description = 'Stores HTTP sessions in a database'
	String documentation = 'http://grails.org/plugin/database-session'
	String group = "RobertFischer"

	String license = 'APACHE'
	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPDATABASESESSION']
	def scm = [url: 'https://github.com/RobertFischer/grails-database-session']

	def dependsOn = [ core: grailsVersion]

	def getWebXmlFilterOrder() {
		// make sure the filter is first
		[sessionProxyFilter: -100]
	}

	def doWithWebDescriptor = { xml ->
		if (!isEnabled(application.config)) {
			return
		}

		def contextParam = xml.'context-param'
		contextParam[contextParam.size() - 1] + {
			'filter' {
				'filter-name'('sessionProxyFilter')
				'filter-class'(DelegatingFilterProxy.name)
			}
		}

		def filter = xml.'filter'
		filter[filter.size() - 1] + {
			'filter-mapping' {
				'filter-name'('sessionProxyFilter')
				'url-pattern'('/*')
				'dispatcher'('ERROR')
				'dispatcher'('FORWARD')
				'dispatcher'('REQUEST')
			}
		}
	}

	def doWithSpring = {
		if (!isEnabled(application.config)) {
			return
		}

		sessionMemoryPersister(InMemoryPersister)

		sessionJdbcMemoryPersister(JdbcPersister) {
			transactionTemplate = { TransactionTemplate tmp ->
				isolationLevelName = "ISOLATION_DEFAULT"
				propagationBehaviorName = "PROPAGATION_NEVER"
				transactionManager = ref("transactionManager")
			}
			jdbcTemplate = { JdbcTemplate tmp -> 
				def dbConfig = tryToFindDbConfig(application.config)
				if(dbConfig) {
					dataSource = { BasicDataSource ds ->
						if(dbConfig.driverClassName) driverClassName = dbConfig.driverClassName
						if(dbConfig.url) url = dbConfig.url
						if(dbConfig.username) username = dbConfig.username
						if(dbConfig.password) password = dbConfig.password
					}
				} else {
					dataSource = ref("dataSourceUnproxied")
				}
			}
		}

		sessionPersister(ChainPersister) {
			persisters = [ ref("sessionMemoryPersister"), ref("sessionJdbcMemoryPersister") ]				
		}

		sessionProxyFilter(SessionProxyFilter) {
			persister = ref('sessionPersister')
            exclusionList = application.config?.grails?.plugin?.databasesessionflushable?.exclusionList ? application.config?.grails?.plugin?.databasesessionflushable?.exclusionList : new String[0]
		}
	}

	def doWithApplicationContext = { appCtx ->
	}

	private static def tryToFindDbConfig(config) {
		def dbConfig = config.grails.plugin.databasesessionflushable.dbConfig
		if(dbConfig.url instanceof String) return dbConfig
		return null
	}

	private static boolean isEnabled(config) {
		def enabled = config.grails.plugin.databasesessionflushable.enabled
		if (enabled instanceof Boolean) {
			return enabled
		}
		return !Environment.isDevelopmentMode()
	}
}
