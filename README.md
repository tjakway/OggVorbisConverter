REQUIREMENTS:
* vlc on PATH
* lltag on PATH
* exiftool

    To install dependencies on Debian & derivatives (including Ubuntu):

    ```sudo apt-get install vlc lltag libimage-exiftool-perl```


DO:
* use scopt for real argument handling
* add flags to pass additional arguments to external tools:
  * --vlc-args
  * --lltag-args
  * --exiftool-args
* it would really be better to handle exiftool output using xml...
* after converting and tagging, run lltag -S against the output ogg files to check them
