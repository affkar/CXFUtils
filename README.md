# CXFUtils
CXF Utils for Karaf - Performance Interceptors

This project includes few interceptors(defined in a osgi bundle) whose interfaces can be added to other osgi bundles that contain CXF based JAXWS webservices.



for 2.7.0 cxf and 2.12.0 fuse, just wrap install the poi jar.{
	<cxf.version>2.7.0.redhat-610379</cxf.version>
	<camel.version>2.12.0.redhat-610379</camel.version>
        <cxf-codegen.version>3.0.3</cxf-codegen.version>
	install -s mvn:org.karthick.performance/poi/1.0.0-SNAPSHOT/

also there are dependencies on cxf-api
XMLUtils vs XMlPrettyPrinter (cxf-core)
}
for 3.0.4 cxf and 2.15.1 fuse, follow instructions as below.{
	<cxf.version>3.0.4.redhat-621084</cxf.version>
        <osgi.version>4.2.0</osgi.version>
        <camel.version>2.15.1.redhat-621084</camel.version>
        <cxf-codegen.version>${cxf.version}</cxf-codegen.version>
It is a pain in the ass getting poi into karaf until I found this

git clone https://github.com/stempler/bnd-platform-minimal.git
cd bnd-platform-minimal
git checkout sample-poi
./gradlew clean bundle
cd build/plugins
#all the osgi bundles should be in there.
install stax api, xmlbeans , poi, commons-codec
}
#features:addurl mvn:org.apache.cxf.karaf/apache-cxf/2.7.0.redhat-611454/xml/features
#features:install cxf-core/2.7.0.redhat-611454
#features:addurl mvn:org.apache.camel.karaf/apache-camel/2.12.0.redhat-610379/xml/features
#features:install camel-cxf/2.12.0.redhat-610379
General instructions common to both 3.0.4 and 2.7.0 cxf/fuse
install -s mvn:org.karthick.performance/CXFPerformanceInterceptors-API/1.0.0-SNAPSHOT/
install -s mvn:org.karthick.performance/CXFPerformanceInterceptors/1.0.0-SNAPSHOT/
install -s mvn:org.karthick.performance/SampleServices/1.0.0-SNAPSHOT/
install -s mvn:org.karthick.performance/CXFPerformanceInterceptors-Dummy/1.0.0-SNAPSHOT/

copy the property file and the xlsx file to fuse etc
add a system property called karaf.etc
and import the soapui project
