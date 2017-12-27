REQUIREMENTS:
* vlc on PATH
* lltag on PATH
* exiftool

    To install dependencies on Debian & derivatives (including Ubuntu):

    ```sudo apt-get install vlc lltag libimage-exiftool-perl```


TODO:
* use scopt for real argument handling
* add flags to pass additional arguments to external tools:
  * --vlc-args
  * --lltag-args
  * --exiftool-args
* it would really be better to handle exiftool output using xml...
* after converting and tagging, run lltag -S against the output ogg files to check them
* just copy .ogg files (re-encoding them lowers quality)
* should have the temp dir be a CLI option and default to either the system temp dir (if it supports atomic moves to the destination directory) or a generated temp dir on that partition
