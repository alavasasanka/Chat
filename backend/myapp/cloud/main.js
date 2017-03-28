Parse.Cloud.afterSave("ChatMessages", function (request) {
	var query = new Parse.Query(Parse.User);
	var chatId = request.object.id;
	var receiversArray = request.object.get("receivers");
	var senderType = request.object.get("senderType");
	var chatMessage = request.object.get("message").toString();
	var senderId = request.object.get("senderId");
	var senderName = request.object.get("senderName");
	var productId = request.object.get("productId");
	var createdTime = request.object.get("createdTime");
	var createdAt = request.object.createdAt;
	
	if (!receiversArray) {
		query.equalTo("objectId", senderId);
		query.first({
			success: function(result) {
				console.log("Success 1st:" + JSON.stringify(result) + result.get("type"));
				var newQuery = new Parse.Query(Parse.User);
				var array = new Array();
				newQuery.withinKilometers("location", result.get("location"), 20);
				newQuery.equalTo("type", "Vendor");
				newQuery.find({
					success: function(results) {
						console.log("Success 2nd:" + JSON.stringify(results));
						for (var i = 0; i < results.length; i++) {
							console.log("Success 2nd:" + JSON.stringify(results[i]));
							array.push(results[i].id);
						}
						var pushQuery = new Parse.Query(Parse.Installation);
						pushQuery.containedIn("userObjectId", array);
						pushQuery.equalTo("deviceType", "android");
						Parse.Push.send({
							where: pushQuery,
							data: {
								alert: chatMessage,
								objectId: chatId,
								senderId: senderId,
								senderType: senderType,
								senderName: senderName,
								message: chatMessage,
								productId: productId,
								createdTime: createdTime,
								createdAt: createdAt,
								receivers: array
							}
						}, {
							success: function() {
								var finalQuery = new Parse.Query("ChatMessages");
								finalQuery.equalTo("objectId", chatId);
								finalQuery.first({
									success: function(result) {
										console.log("Success 3rd:" + JSON.stringify(result));
										result.set("receivers", array);
										result.save();
									},
									error: function(error) {
										console.error("error 4th: " + JSON.stringify(error));
									}
								});
								console.log("Push was successful");
							},
							error: function(error) {
								console.error("error 3rd: " + JSON.stringify(error));
							}
						});
					},
					error: function(error) {
						console.log("error 2nd: " + JSON.stringify(error));
					}
				});
			},
			error: function(error) {
				console.log("error 1st: " + JSON.stringify(error));
			}
		});
	} else {
		console.log("In else case");
		if (senderType == "Vendor") {
			var pushQuery = new Parse.Query(Parse.Installation);
			pushQuery.containedIn("userObjectId", receiversArray);
			pushQuery.equalTo("deviceType", "android");
			Parse.Push.send({
				where: pushQuery,
				data: {
					alert: chatMessage,
					objectId: chatId,
					senderId: senderId,
					senderType: senderType,
					senderName: senderName,
					message: chatMessage,
					productId: productId,
					receivers: receiversArray,
					createdTime: createdTime,
					createdAt: createdAt
				}
			}, {
				success: function() {
					console.log("Push was successful");
				},
				error: function(error) {
					console.error("error: " + JSON.stringify(error) + " Push failed");
				}
			});
		}
	}
});

Parse.Cloud.define("sendMessage", function(request, status) {
    var objectsToSave = [];
    var parseObject = Parse.Object.extend("ChatMessages");
    var object = new parseObject();
    object.set("message",request.params["message"]);
    object.set("productId",request.params["productId"]);
    object.set("senderId",request.params["senderId"]);
    object.set("senderType",request.params["senderType"]);
	object.set("senderName",request.params["senderName"]);
	object.set("createdTime",request.params["createdTime"]);
	if (request.params["receivers"])
    	object.set("receivers",request.params["receivers"]);
    objectsToSave.push(object);
    Parse.Object.saveAll(objectsToSave, {
        success: function(saveList) {
            status.success(saveList[0]);
        },
        error: function(error) {
            status.error("Unable to save objects.")
        }
    });
});
