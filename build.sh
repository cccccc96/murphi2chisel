git clone git@github.com:aman-goel/avr.git avr-master
cd avr-master
./build.sh
cd ..

cd chiselFV
sbt
cd ..

sudo apt-get install yosys

pip install lark


