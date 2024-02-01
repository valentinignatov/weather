This project is a small weather forecast application using the public openweathermap weather API made with spring-boot
to run this application:
	make sure Java 17 is installed on your machine and JAVA_HOME has path to it
	clone this repository
	open console
	navigate to this project directory
	run 'java -jar weather-0.0.1-SNAPSHOT.jar'

after successful startup:
	you can test the following endpoints
	
	POST
	http://localhost:8080/mydomain/app/weather
	{
		"postalcode": "77391"
	}

	GET
	http://localhost:8080/mydomain/app/history?postalcode=77391

Thank you for using our application!