
	$(document).ready(function(){
		$("#seasons").click(function(){
			$("#winter").fadeToogle();
			$("#summer").fadeToogle("slow");
			$("#fall").fadeToogle(3430);
		});
	});

	$(document).ready(function(){
    $("#flip").click(function(){
        $("#panel").slideDown("slow");
    });
});

	$(document).ready(function(){
    	$("#besthide").click(function(){
        	$("#onlybuiltout").hide(1000);
    	});
    	$("#bestalbum").click(function(){
        	$("#onlybuiltout").show(1000);
    	});
	});



var car = {
	type:"Audi", 
	model:"A5", 
	color:"Red", 
	getProperties: function(){
		return this.type + " " + this.model + " " + this.color;
		}
	};

document.getElementById("mycarprop").innerHTML = car.getProperties();







function protossAwake(){
		var i = document.getElementById('protossdark');
		if(i.src.match("protossdark")){
			i.src = "images/pic_protosslight.jpg";
		} else {
			i.src = "images/pic_protossdark.png";
		}
	}

	function changeStyle(){
		var j = document.getElementById('style');
		j.style.fontSize = '25px';
		j.style.color = 'white';
	}

	function numbCheck(){
		var text, x;
		x = document.getElementById("numbcheckinput").value;
		if (isNaN(x) || x < 0 || x > 20) {
			text = "input is no good try again";
			window.alert("Try a number between 0 to 20");
		} else {
			text = "congratulations you can count";
		}
		document.getElementById("numbcheckoutput").innerHTML = text;
	}



	function multNumb(){
		var m1msg, x, y;
		x = document.getElementById("mult1").value;
		y = document.getElementById("mult2").value;
		m1msg = document.getElementById("m1msg");
		m1msg.innerHTML = '';
		try{
			if (x == "" || y == "") {
				window.alert("input is empty, please enter a number");
				throw "is empty";
			}
			else if (isNaN(x) || isNaN(y)) {
				window.alert("input is not a number");
				throw "not a number";
			}
			else {
				z = x * y;
			}
		}
		catch(err){
			m1msg.innerHTML = "Input " + err;
		}
		document.getElementById("M1M2").innerHTML = z;
	}

	

	function toCelsius(){
		var x;
		x = document.getElementById("enterfahr").value;
		x = (5/9) * (x-32);
		try{
			if (x == "") {
				window.alert("input is empty, please enter a number");
				throw "is empty";
			}
			else if (isNaN(x)) {
				window.alert("input is not a number");
				throw "not a number";
			}
			document.getElementById("fahrout").innerHTML = "the fahrenheit is " + x +" celsius";
		}
		catch(err){
			document.getElementById("fahrout").innerHTML = "input " + err;
		}
	}

	function countVowels(){
		var str = document.getElementById("stringvowel").value;
		var count = str.match(/[aeiou]/gi).length;
		if(count > 0){
			document.getElementById("msg2").innerHTML = count;
		}
		else {
			document.getElementById("msg2").innerHTML = "invalid input, try agian";
			window.alert("input is invalid");
		}
	}

	function stringToVert(){
		var str = document.getElementById("stringvert").value;
		var arr = str.split("");
		var output = "";
		var i;
		for(i = 0; i < arr.length; i++){
			output += arr[i] + "<br>";
		}
		document.getElementById("stringvertout").innerHTML = output;
	}

	function reverseNumb(){
		var str = document.getElementById("revnums").value;
		var arr = str.split("");
		var output = "";
		var i;
		for(i = arr.length - 1; i >= 0; i--){
			output += arr[i] + " ";
		}
		document.getElementById("revnumsout").innerHTML = output;
	}
			

	function writeToBlank(){
		var j = document.getElementById('blank');
		document.write("blank documnet starts here");
		console.log(5+6);
	}



	function changeFullMoon(){
		var halfmoon = "&#9680;&#9680;&#9680;&#9680;&#9680;&#9680;";
		var fullmoon = "&#9679;&#9679;&#9679;&#9679;&#9679;&#9679;";

		while(document.getElementById("fullMoon").innerHTML = fullmoon){
			document.getElementById("fullMoon").innerHTML = halfmoon;
		}
			document.getElementById("fullMoon").innerHTML = fullmoon;
		}



