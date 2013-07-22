for path in follower friend profile tweet friendList
do
    mkdir $path
    for dir in {0..9}
    do
	mkdir $path'/'$dir
    done

    for dir in {a..z}
    do
	mkdir $path'/'$dir
    done

    mkdir $path'/_'
done
