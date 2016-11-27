FROM openjdk:8-jdk
RUN wget -q https://dl.bintray.com/sbt/debian/sbt-0.13.12.deb
RUN dpkg -i sbt-0.13.12.deb
RUN wget https://bootstrap.pypa.io/get-pip.py
RUN python get-pip.py
RUN pip install pip
RUN pip install awscli
RUN sbt
WORKDIR /code
