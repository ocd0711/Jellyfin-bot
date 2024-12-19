#!/bin/bash

# Variables
imdb_pattern='^tt[0-9]*$'
webhook_url='http://127.0.0.1:8081'

webhook() {
  # curl -X POST "$webhook_url/emby/medialtemAddedComplete?token=238620" -H "Content-Type: application/json" -d "$1"
  wget --post-data="$1" --header="Content-Type: application/json" "$webhook_url/emby/medialtemAddedComplete?token=238620"
}

#"%item.type%" "%item.name%" "%item.productionyear%" "%item.meta.imdb%" "%item.meta.tmdb%" "%season.name%" "%episode.number%" "%series.name%" "%series.meta.imdb%" "%series.meta.tmdb%" "%item.id%"
if [[ $1 == Movie ]]; then
  webhook "{\"type\":\"$1\",\"movie\":\"$2\",\"year\":\"$3\",\"imdb\":\"$4\",\"tmdb\":\"$5\",\"season\":\"$6\",\"episode\":\"$7\",\"id\":\"${11}\"}"
else
  webhook "{\"type\":\"$1\",\"movie\":\"$8\",\"year\":\"$3\",\"imdb\":\"$9\",\"tmdb\":\"${10}\",\"season\":\"$6\",\"episode\":\"$7\",\"id\":\"${11}\"}"
fi

#if [[ $1 == Movie ]] && [[ "$4" =~ $imdb_pattern ]]; then
#if [[ $1 == Movie ]]; then
#  # Check if item.type is Movie and item.meta.imdb is set
#  webhook "{\"type\":\"$1\",\"movie\":\"$2\",\"year\":\"$3\",\"imdb\":\"$4\",\"tmdb\":\"$5\",\"library\":\"$6\"}"
##elif [[ $1 == Series ]] && [[ "$3" =~ $imdb_pattern ]]; then
#elif [[ $1 == Series ]]; then
#  # Check if item.type is Series and item.meta.imdb is set
#  webhook "{\"type\":\"$1\",\"series\":\"$2\",\"imdb\":\"$3\",\"tmdb\":\"$4\",\"library\":\"$5\"}"
##elif [[ $1 == Season ]] && [[ "$4" =~ $imdb_pattern ]]; then
#elif [[ $1 == Season ]]; then
#  # Check if item.type is Season and item.meta.imdb is set
#  webhook "{\"type\":\"$1\",\"season\":\"$2\",\"series\":\"$3\",\"imdb\":\"$4\",\"tmdb\":\"$5\"}"
##elif [[ $1 == Episode ]] && [[ "$5" =~ $imdb_pattern ]]; then
#elif [[ $1 == Episode ]]; then
#  # Check if item.type is Season and item.meta.imdb is set
#  webhook "{\"type\":\"$1\",\"episode\":\"$2\",\"season\":\"$3\",\"series\":\"$4\",\"imdb\":\"$5\",\"tmdb\":\"$6\"}"
#fi
