#!/usr/bin/env bash


USAGE="conv-music-ogg-vorbis.sh [input-dir] [output-dir]"

if [ ! -d "$1" ]; then
    echo "$USAGE"
    exit 1
fi

if [ ! -d "$2" ]; then
    echo "$USAGE"
    exit 1
fi

#see https://stackoverflow.com/questions/592620/check-if-a-program-exists-from-a-bash-script
function command_exists {
    type "$1" &> /dev/null ;
}

command_exists vlc || ( echo "Command vlc does not exist" ; exit 1 )

INPUT_DIR="$1"
DEST_DIR=$(realpath "$2")


function rm-suffix {
    if [[ -z "$1" ]]; then
        echo "rm-suffix called with no argument"
        exit 1
    fi

    #see https://stackoverflow.com/questions/125281/how-do-i-remove-the-file-suffix-and-path-portion-from-a-path-string-in-bash
    RM_SUFFIX_RESULT=${1%.*}
}

#takes 1 argument
#and requires DEST_DIR be set
function conv-vlc-ogg-vorbis {

    if [[ -z "$1" ]]; then
        echo "conv-vlc-ogg-vorbis called with no argument"
        exit 1
    fi

    #***codec variables***
    #see https://wiki.videolan.org/Codec/

    #vorbis codec
    local ACODEC=vorb

    #mux into ogg for ogg vorbis
    local MUX=ogg

    #try not including this
    #ogg vorbis is not supposed to use a set bitrate,
    #see https://grahammitchell.com/writings/vorbis_intro.html#a_bit_on_bitrates
    #local AUDIO_BITRATE=256
    #*********************


    #***building output path***
    #converting to ogg vorbis
    local EXT="ogg"

    rm-suffix $(basename "$1")
    local FILE_BASENAME="$RM_SUFFIX_RESULT"
    local DST_FILE_FULL_PATH="$DEST_DIR/$FILE_BASENAME.$EXT"

    if [[ -f "$DST_FILE_FULL_PATH" ]]; then
        echo "WARNING: $DST_FILE_FULL_PATH already exists, overwriting..."
    fi

    #**************************

    #do the actual transcoding


    echo "FILE_BASENAME: $FILE_BASENAME"
    echo "DST_FILE_FULL_PATH: $DST_FILE_FULL_PATH"

    #see https://wiki.videolan.org/Transcode/
    #-I dummy disables GUI
    #vlc -I dummy -vvv "$1" --sout "#transcode{acodec=$ACODEC}:standard{mux=$MUX,\
    #    dst=\"${DST_FILE_FULL_PATH}\",access=file}" vlc://quit
}

function conv-find-cmd {
    if [[ -z "$1" ]]; then
        echo "conv-find-cmd called with no argument"
        exit 1
    fi


    #the first bash argument will be assigned to $0
    #don't forget: -regextype has to come BEFORE the regex
    find "$1" -type f \
        -regextype egrep \
        -iregex ".*.(mp3|ogg|flac|m4a|mp2|opus|m4v)" \
        -exec bash -c "conv-vlc-ogg-vorbis $0" {} \;
}

#need to export all functions to the subshell
export -f rm-suffix
export -f conv-vlc-ogg-vorbis


conv-find-cmd "$INPUT_DIR"
