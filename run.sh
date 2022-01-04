#!/bin/bash
SCRIPT_DIR="$(dirname "$0")"
LOGFILE="$SCRIPT_DIR/beer.log"
RECIPIENTS="example@gmail.com"

fetchEvents() {
	java -jar $SCRIPT_DIR/beertasting-finder-0.0.2.jar --html
}
notifyEventsFound() {
	message="$1"
	subject="Beertasting events found!"
	echo -e "$message" | mailx -s "$subject" -a 'Content-Type: text/html' $RECIPIENTS
}
log() {
	echo -e "$(date +'%F %T') | $@" >> "$LOGFILE"
}

log "Fetching events"
events=$(fetchEvents)

if [ -n "$events" ]; then
	log "Events found: \n$events"
	notifyEventsFound "$events"
else
	log "No events found"
fi

