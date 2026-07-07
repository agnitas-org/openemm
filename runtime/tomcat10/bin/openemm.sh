#!/bin/bash

start() {
	echo "Starting ..."
	/home/openemm/bin/backend.sh start
	/home/openemm/bin/emm.sh start
}

stop() {
	echo "Stopping ..."
	/home/openemm/bin/emm.sh stop
	/home/openemm/bin/backend.sh stop
}

restart() {
	stop
	start
}

status() {
	echo "Status:"
	/home/openemm/bin/emm.sh status
	/home/openemm/bin/backend.sh status
}

case "$1" in
	start)
		start
		;;
	status)
		status
		;;
	restart)
		restart
		;;
	stop)
		stop
		;;
	*)
		echo "Error, permitted parameters are:"
		echo "${0} (start | stop | restart | status)"
		exit 2
		;;
esac
exit 0
