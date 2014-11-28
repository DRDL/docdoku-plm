/*global define,App*/
define(
    [
        "backbone",
        "mustache",
        "views/shortcuts_view",
        "text!templates/control_modes.html"
    ], function (Backbone, Mustache, ShortcutsView, template) {

        var ControlModesView = Backbone.View.extend({

            className: "side_control_group",

            events: {
                "click button#flying_mode_view_btn": "flyingView",
                "click button#tracking_mode_view_btn": "trackingView",
                "click button#orbit_mode_view_btn": "orbitView",
                "click button#oculus_mode_view_btn": "oculusView",
            },

            flyingView: function () {
                App.sceneManager.setPointerLockControls();
            },

            trackingView: function () {
                App.sceneManager.setTrackBallControls();
            },

            orbitView: function () {
                App.sceneManager.setOrbitControls();
            },

            oculusView: function () {
                App.sceneManager.setFirstPersonControls();
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                this.shortcutsview = new ShortcutsView().render();
                this.$(".nav-header").after(this.shortcutsview.$el);
                return this;
            }

        });

        return ControlModesView;

    });
