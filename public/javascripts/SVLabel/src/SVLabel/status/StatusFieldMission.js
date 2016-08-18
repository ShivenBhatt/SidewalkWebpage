function StatusFieldMission (modalModel, uiStatusField) {
    var self = this;
    var $missionDescription = uiStatusField.holder.find("#current-mission-description");

    // These are messages that are shown under the "Current Mission" in the status pane. The object's keys correspond to
    // the "label"s of missions (e.g., "initial-mission"). Substitute __PLACEHOLDER__ depending on each mission.
    var missionMessages = {
        "onboarding": "Complete the onboarding tutorial!",
        "initial-mission": "Walk for 1000ft and find all the sidewalk accessibility attributes",
        "distance-mission": "Audit __PLACEHOLDER__ of this neighborhood.",
        "area-coverage-mission": "Make the __PLACEHOLDER__ of this neighborhood accessible"
    };

    modalModel.on("ModalMissionComplete:closed", function (param) {
        self. setMessage(param.nextMission);
    });

    /**
     * This method returns the mission message based on the passed label parameter.
     * @param label {string} Mission label
     * @returns {string}
     */
    this._getMissionMessage = function (label) {
        return label in missionMessages ? missionMessages[label] : "";
    };

    /**
     * This method takes a mission object and sets the appropriate text for the mission status field in 
     * the status pane.
     * @param mission
     * @returns {printMissionMessage}
     */
    this.setMessage = function (mission) {
        var missionLabel = mission.getProperty("label");
        var missionMessage = this._getMissionMessage(missionLabel);

        if (missionLabel == "distance-mission") {
            var distanceString = this._auidtDistanceToString(mission.getProperty("distanceMi"), "miles");
            missionMessage = missionMessage.replace("__PLACEHOLDER__", distanceString);

        } else if (missionLabel == "area-coverage-mission") {
            var coverage = (mission.getProperty("coverage") * 100).toFixed(0) + "%";
            missionMessage = missionMessage.replace("__PLACEHOLDER__", coverage);
        }

        $missionDescription.html(missionMessage);

        return this;
    };
}

StatusFieldMission.prototype._auidtDistanceToString = function (distance, unit) {
    if (!unit) unit = "kilometers";

    if (unit == "miles") {
        if (distance <= 0.20) {
            return "1000ft";
        } else if (distance <= 0.25) {
            return "&frac14;mi";
        } else if (distance <= 0.5) {
            return "&frac12;mi"
        } else if (distance <= 0.75) {
            return "&frac34;mi";
        } else {
            return distance.toFixed(0, 10) + "";
        }
    } else if (unit == "feet") {
        return distance + "";
    } else {
        return distance + "";
    }
};