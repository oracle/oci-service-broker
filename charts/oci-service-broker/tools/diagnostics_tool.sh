#!/bin/bash

##### Initialize Variables
OSB="OCI Service Broker"
KGP="kubectl get pods"


#Function for Priting the Start of Diagnostic Tool
function start_tool()
{
	echo -e "\n===============================Starting OCI Service Broker Diagnostic Tool=======================================\n\n\n";
}

#Function for Printing the Stop of Diagnostic Tool
function stop_tool()
{
	echo -e "\n\n\n===============================OCI Service Broker Diagnostic Tool Completed======================================";
}

#Function for Border Print
function border_print()
{
	echo -e "\n******************************************************************************************************************"
}

#Function for Collecting logs
#Parameter Details:
#$1 - Name of the OCI Service Broker
#$2 - OCI Service Broker name in the Helm Release
#$3 - OCI Service Broker namespace
#$4 - Service Catalog namespace
#$5 - OCI Service Broker Pod Name
function collect_logs ()
{
	border_print
    echo -e "\n\nCollecting $1 Related Logs : "
	fol_name="OSB-Logs"-$(date "+%Y%m%d-%H%M%S")
	fol="/tmp/"$fol_name
    mkdir $fol
    # OCI Service Broker Logs
    kubectl -n $3 logs $5 -c oci-service-broker > $fol/osb_pod.log
    # Helm ls logs
    helm ls $2 --namespace $3 > $fol/helm_ls.log
    # Helm Values Changed Logs
    helm get values $2 > $fol/helm_values_changed.log
    # Kubectl get all for OCI Service Broker Namespace Logs
    kubectl get all -n $3 > $fol/kubectl_get_all_osb_ns.log
    # Kubectl get all for Catalog Namespace Logs
    kubectl get all -n $4 > $fol/kubectl_get_all_catalog_ns.log
    # svcat for brokers, instances, bindings Logs
    svcat get brokers -n $3 -o yaml > $fol/svcat_brokers.log
    svcat get instances -n $3 > $fol/svcat_instances.log
    svcat get bindings -n $3 > $fol/svcat_bindings.log
    # Kubectl describe OCI Service Broker Logs
    kubectl -n $3 describe pod $5 > $fol/osb_pod_describe.log
    zip -qq -r $fol.zip $fol/
    rm -rf $fol/
	echo -e "The Logs are available at " $fol.zip ". Please share this zip file with OCI Service Broker dev team."
}

function usage_info()
{
	echo -e "################################################################################################################\n"
	echo "OCI Service Diagnostics Tool needs 2 arguments i.e., OCI Service Broker Namespace and Service Catalog Namespace"
	echo -e "\nUsage:\n ./osb_diagnostics.sh <OCI-Service-Broker-Namespace> <Service-Catalog-Namespace>\n"
	echo -e "################################################################################################################\n"
}

#############################################################################################################################
#### Starting of the OSB Diagnostic Tool
start_tool

if [ "$1" == "-h" ] || [ "$1" == "--help" ]
then
	usage_info
	exit 0
elif [ $# -le 1 ]
then
	usage_info
	exit 1
fi

##### Checking the Kube Config Env Variable
if [ -z "$KUBECONFIG" ]
then
	echo -e "KUBECONFIG environment variable is not set. Hence this tool will use the default kube config."
else
	echo -e "Output of KUBECONFIG env variable : " $KUBECONFIG
fi

svcat_ver=`svcat version`
if [ -z "$svcat_ver" ]
then
	echo -e "\nsvcat tool is not installed to verify whether Service Catalog is running and able to connect to $OSB\n"
	echo -e "Please follow the instructions in https://svc-cat.io/docs/install/#installing-the-service-catalog-cli to install svcat tool"
	stop_tool
	exit 1
fi

### Service Catalog Related Diagnostics
CATALOG_NS=$2
OSB_NS=$1
ctlg_pods=($($KGP -n $CATALOG_NS | grep catalog | xargs))
if [  -z "$ctlg_pods" ]
then
	echo -e "\nService Catalog is not running in the given Namespace. Exiting the Tool."
	stop_tool
	exit 1;
fi
border_print
echo -e "\nBelow are the Service Catalog pods:\n"
$KGP -n $CATALOG_NS | grep catalog

### OCI Service Broker Related Diagnostics
osb_pod_out=$($KGP -n $OSB_NS | grep "oci-service-broker" | head -1)
if [ -z "$osb_pod_out" ]
then
	echo -e "\n$OSB is not running in the given Namespace. Exiting the Tool."
	stop_tool
	exit 1;
fi
border_print
echo -e "\nBelow are the $OSB pods: \n"
$KGP -n $OSB_NS | grep "oci-service-broker"

osb_name=$(echo $osb_pod_out | awk '{split($0,a," "); print a[1]}' | awk '{split($0,b,"-oci-service-broker-"); print b[1]}')
osb_pod_status=$(echo $osb_pod_out | awk '{split($0,a," "); print a[3]}')
osb_pod_name=$(echo $osb_pod_out | cut -d" " -f1)

catalog_name=$($KGP -n $CATALOG_NS | grep "catalog-apiserver" | awk '{split($0,a," "); print a[1]}' | awk '{split($0,b,"-catalog-apiserver-"); print b[1]}')

##### Helm List
border_print
echo -e "Helm List of OCI Service Broker\n"
helm ls $osb_name --namespace=$OSB_NS
echo -e "\nHelm List of Service Catalog\n"
helm ls $catalog_name --namespace=$CATALOG_NS

if [ ! -z $osb_pod_status ]
then
	if [ $osb_pod_status = "ContainerCreating" ]
	then
		oci_cred_miss=`kubectl -n $OSB_NS describe pod $osb_pod_name | grep 'secrets "ocicredentials" not found'`
		if [ ! -z oci_cred_miss ]
		then
			echo -e -n "\nOCI Credentails Kubernetes secret is Missing. Follow link --> (https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/installation.md#oci-credentials) "
			echo -e "to create the user credentials with valid information.\n"
		fi
	elif [ $osb_pod_status = "CrashLoopBackOff" ]
	then
		echo -e "\nThe OCI Service Broker Pod is in CrashLoopBackOff State."
		auth_err_msg=$(kubectl -n $OSB_NS logs $osb_pod_name -c oci-service-broker | grep "Authentication failed")
		if [ ! -z "$auth_err_msg" ]
		then
			echo -e "\nOCI Credentials Kubernetes secret: \n"
			kubectl get secret ocicredentials -n $OSB_NS
			echo -e -n "\nOCI Credentials Kubernetes secret is having Invalid credentails. Follow link --> (https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/installation.md#oci-credentials) "
			echo -e "to create the user credentials with valid information.\n"
		fi
		collect_logs "$OSB" "$osb_name" "$OSB_NS" "$CATALOG_NS" "$osb_pod_name"
		stop_tool
		exit 0;
	fi
fi

border_print
echo -e "\nsvcat get brokers Output:\n"
svcat get brokers

svcat_brk_out=`svcat get brokers | awk 'NR>2 { print $1}'`
brk_count=$(($(svcat get brokers | wc -l) -2))
if [ $brk_count -gt 1 ]
then
	echo -e "\nTotal number of OCI Service Brokers registered is : " $brk_count
	echo -e "If multiple OCI Service Brokers is registered, only one broker can be in Ready State.\n"
elif [ $brk_count -eq 0 ]
then
	echo -e "\n$OSB is not yet registered with Service Catalog. Please use the yaml at charts/oci-service-broker/samples/oci-service-broker.yaml to register\n"
fi
q=1
while [ $q -le $brk_count ] 
do
	border_print
	brk_name=$(echo $svcat_brk_out | cut -d " " -f $q)
	echo ""
	echo -e "Broker : " $brk_name "  \nStatus : \n"
	svcat_brk_status=($(svcat get broker $brk_name | grep "$brk_name" | awk '{split($0,a," "); print a[3] }' ))
	osb_url=`svcat get broker $brk_name | grep "$brk_name" | awk '{split($0,a," "); print a[2] }'`

	if [ $svcat_brk_status = "Ready" ]
	then
		echo -e "$OSB is successfully installed and able to communicate with Service Catalog"
	elif [ $svcat_brk_status = "ErrorFetchingCatalog" ]
	then
		echo -e "$OSB is unable to communicate with the Service Catalog."
		if [ $CATALOG_NS != $OSB_NS ]
		then
			echo -e "\nService Catalog and $OSB are in different Namespaces."
		fi
		echo -e "Please check the URL ($osb_url) used in oci-service-broker.yaml is valid and re-register based on documentation."
	fi
	q=$(($q + 1))
done

collect_logs "$OSB" "$osb_name" "$OSB_NS" "$CATALOG_NS" "$osb_pod_name"
stop_tool



