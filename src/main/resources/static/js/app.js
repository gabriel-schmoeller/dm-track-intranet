(function() {
    function padTime(num) {
        var s = "00" + num;
        return s.substr(s.length-2);
    }

    var app = angular.module('timeTracking', ['ui.bootstrap', "oitozero.ngSweetAlert", 'ui.select', 'ngSanitize'])
    .config(['$httpProvider', function($httpProvider) {
      $httpProvider.defaults.withCredentials = true;
      $httpProvider.interceptors.push(function($q, SweetAlert, $rootScope) {
        return {
            'responseError': function(response) {
                if (response.status == 403) {
                    $rootScope.$broadcast('login.forbidden', '');
                } else if (response.status == 500) {
                    swal("Erro", response.data.message, "error");
                } else if (response.status == 400) {
                    swal("Atenção", response.data.message, "warning");
                }

                return $q.reject(response);
            }
        };
      });

      $httpProvider.interceptors.push(function ($q, $rootScope, $log, userService) {
          var numLoadings = 0;
          return {
              request: function (config) {
                  if (userService.getToken()) {
                      config.headers['x-auth-token'] = userService.getToken();
                  }
                  numLoadings++;
                  $rootScope.$broadcast("loader_show");
                  return config || $q.when(config)
              },
              response: function (response) {
                  if ((--numLoadings) === 0) {
                      $rootScope.$broadcast("loader_hide");
                  }
                  return response || $q.when(response);
              },
              responseError: function (response) {
                  if (!(--numLoadings)) {
                      $rootScope.$broadcast("loader_hide");
                  }
                  return $q.reject(response);
              }
          };
      });
    }]);

    app.service('userService', function() {
        var currentUser = "";
        var authToken = "";

        this.setToken = function(token) {
            authToken = token;
            localStorage.setItem('token', authToken);
        }

        this.getToken = function() {
            if (!authToken) {
                authToken = localStorage.getItem('token');
            }

            return authToken
        }
    });

    app.controller('MainController',
            ['$scope', "$http", "$uibModal", "$interval", "userService",
             function($scope, $http, $uibModal, $interval, userService) {
        // ++++++++++++++++++++++++++++++++++++++++++++++++++Auxiliares

        function updateTodayTime() {
            if ($scope.today.timeTracks != undefined) {
                var total = 0;
                var tracks = $scope.today.timeTracks;

                for (var i = 0; i < tracks.length; i++) {
                    if ((i+1)%2===0) {
                        var t2 = tracks[i].split(":");
                        var t1 = tracks[i-1].split(":");
                        total += (parseInt(t2[0])*60 + parseInt(t2[1])) - (parseInt(t1[0])*60 + parseInt(t1[1]));
                    }
                }

                var now = new Date();
                var lastTrack = tracks[tracks.length-1].split(":");
                total += ((now.getHours()*60) + now.getMinutes()) - (parseInt(lastTrack [0])*60 + parseInt(lastTrack [1]));

                var result = padTime(Math.floor(total/60)) + ":" + padTime(total%60);
                $scope.today.todayTime = result;
            }
        }

        $scope.hasIncomplete = function(reports) {
            var hasAny = false;

            for (var i = 0; i < reports.length; i++) {
                if (reports[i].incomplete) {
                    hasAny = true;
                }
            }

            return hasAny;
        }

        $scope.durationClass = function(duration) {
            if (duration === '00:00') {
                return "";
            } else if (typeof duration === 'string' && duration.charAt(0)=="-") {
                return "text-danger";
            } else {
                return "text-success";
            }
        }

        $scope.isWeekend = function(date) {
            var date = new Date(date);
            return date.getDay() == 0 || date.getDay() == 6;
        }

        $scope.isFreeDay = function(report) {
            return report.freeDay && !$scope.isWeekend(report.date);
        }

        $scope.tableRowClass = function(row) {
            if(row.incomplete) {
                return "warning";
            } else if(row.justified != "00:00") {
                return "info";
            } else if(row.balance === "00:00") {
                if ($scope.isWeekend(row.date)) {
                    return "active";
                } else {
                    return "";
                }
            } else if (typeof row.balance === 'string' && row.balance.charAt(0)=="-") {
                return "danger";
            } else {
                return "success";
            }
        }

        $scope.monthName = function(month) {
            var monthName = new Array(12);
            monthName[1]="Jan/Dez";
            monthName[2]="Fev/Jan";
            monthName[3]="Mar/Fev";
            monthName[4]="Abr/Mar";
            monthName[5]="Mai/Abr";
            monthName[6]="Jun/Mai";
            monthName[7]="Jul/Jun";
            monthName[8]="Ago/Jul";
            monthName[9]="Set/Ago";
            monthName[10]="Out/Set";
            monthName[11]="Nov/Out";
            monthName[12]="Dez/Nov";

            return monthName[month];
        }

        $scope.weekDay = function(date) {
            date = new Date(date);
            var weekday=new Array(7);
            weekday[0]="Domingo";
            weekday[1]="Segunda-feira";
            weekday[2]="Terça-feira";
            weekday[3]="Quarta-feira";
            weekday[4]="Quinta-feira";
            weekday[5]="Sexta-feira";
            weekday[6]="Sabado";

            return weekday[date.getDay()];
        }

        function resetFields() {
            $scope.credentials = {user: "", pass: ""};
            $scope.today = {};
            $scope.balanceCalc = {};
            $scope.ftLocalDate = "";
            $scope.localYears = [];
            $scope.username = "";
            $scope.year = 0;
            $scope.actualBalance = {};
            $scope.monthReports = [];
            $scope.timeContract = [];
            $scope.ticketToDay = {date: new Date()};
            $scope.ticketTemplates = [];
        }

        function setActualBalance(data) {
            $scope.actualBalance.initialDay = data.initialDay;
            $scope.actualBalance.initialBalance = data.initialBalance;
            $scope.actualBalance.actualBalance = data.actualBalance;
        }

        function setLocalDates(data) {
            $scope.ftLocalDate = data.ftLocalDate;
            $scope.localYears = data.localYears;

            if ($scope.year === 0) {
                $scope.year = $scope.localYears[0];
            }
        }

        function refreshPage() {
                $scope.requestTodayReport();
                $scope.requestActualBalance();
                $scope.requestTimeContract();
                $scope.requestLocalData($scope.requestDailyReports);
                $scope.requestTicketTemplates();
        }

        function refreshPageIfNeeded() {
            var ftLocalDate = $scope.ftLocalDate;

            $scope.requestLocalData(function() {
               if ($scope.ftLocalDate != ftLocalDate) {
                   $scope.requestTodayReport();
                   $scope.requestActualBalance();
                   $scope.requestTimeContract();
                   $scope.requestDailyReports();
               }
            });
        }
        // -------------------------------------------------------Auxiliares

        // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++Login
        $scope.userSpaced = function(user) {
            return user.replace('.', ' ');
        }
        $scope.login = function() {
            userService.setToken("");
            $http.post("/service/login", $scope.credentials).then(
                function(response) {
                    $scope.username = response.data.username;
                    userService.setToken(response.headers('x-auth-token'));
                    refreshPage();
                }
            );
        }

        $scope.tryLogin = function() {
            $http.get("/service/logged").then(
                function(response) {
                    if (response.data.username) {
                        $scope.username = response.data.username;
                        refreshPage();
                    }
                }
            );
        }

        $scope.logout = function() {
            $http.get("/service/logout").then(
                function(response) {
                    userService.setToken("");
                    resetFields();
                }
            );
        }
        // ------------------------------------------------------Login

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++Painel principal
        $scope.requestTodayReport = function() {
            $http.get("/service/today-report", $scope.credentials).then(
                function(response) {
                    $scope.today.timeTracks = response.data.timeTracks;
                    $scope.today.estimatedEnd = response.data.estimatedEnd;
                    $scope.balanceCalc.ftDate = new Date(response.data.balanceCalcFtDate);
                    $scope.balanceCalc.ltDate = new Date(response.data.balanceCalcLtDate);
                    $scope.balanceCalc.result = response.data.balanceCalcResult;

                    updateTodayTime();
                }
            );
        }

        $scope.requestActualBalance = function() {
            $http.get("/service/actual-balance").then(
                function(response) {
                    setActualBalance(response.data);
                }
            );
        }

        $scope.openUpdateInitialBalance = function() {
            var downMonthsModal = $uibModal.open({
                animation: true,
                templateUrl: 'update-initial-balance.html',
                controller: 'UpdateInitialBalanceController',
                size: "sm",
                resolve: {
                    fields: function () {
                      return $scope.actualBalance;
                    }
                  }
            });

            downMonthsModal.result.then(function (response) {
                setActualBalance(response.data);
                $scope.refreshPageIfNeeded();
            });
        }

        $scope.requestLocalData = function(callBack) {
            $http.get("/service/download-months").then(
                function(response) {
                    setLocalDates(response.data);
                    if (callBack != undefined) {
                        callBack();
                    }
                }
            );
        }

        $scope.openDownMonths = function() {
            var downMonthsModal = $uibModal.open({
                animation: true,
                templateUrl: 'download-months.html',
                controller: 'DownloadMonthsController'
            });

            downMonthsModal.result.then(function (response) {
                setLocalDates(response.data);
                $scope.refreshPage();
            });
        }

        $scope.calcBalance = function() {
            var data = {ftDate: $scope.balanceCalc.ftDate, ltDate: $scope.balanceCalc.ltDate};
            $http.post("/service/calculate-balance", data).then(
                function(response) {
                    $scope.balanceCalc.result = response.data.result;
                    $scope.refreshPageIfNeeded();
                }
            );
        }
        // ------------------------------------------------------Painel principal

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++Tabela de horas
        $scope.requestTimeContract = function() {
            $http.get("/service/time-contract").then(
                function(response) {
                    $scope.timeContract = response.data;
                }
            );
        }

        $scope.requestTicketTemplates = function() {
            $http.get("/service/ticket-types").then(
                function(response) {
                    $scope.ticketTemplates = response.data;
                }
            );
        }

        $scope.requestDailyReports = function() {
            $http.get("/service/daily-reports/"+$scope.year).then(
                function(response) {
                    var active = 0;
                    for	(var i = 0; i < $scope.monthReports.length; i++) {
                        if ($scope.monthReports[i].active) {
                            active = i;
                        }
                    }

                    $scope.monthReports = response.data;
                    $scope.monthReports[active].active = true;
                }
            );
        }

        $scope.updateDate = function(date) {
            $http.post("/service/update-date", {date: date}).then(
                function(response) {
                    if (response.data.hasChange) {
                        refreshPage();
                    }
                }
            );
        }

        $scope.markAsIncomplete = function(date) {
            $http.post("/service/mark-incomplete", {date: date}).then(
                function(response) {
                    refreshPage();
                }
            );
        }

        $scope.markAsComplete = function(date) {
            $http.post("/service/mark-complete", {date: date}).then(
                function(response) {
                    refreshPage();
                }
            );
        }

        $scope.addNotePopover = {
            templateUrl: 'add-note-template.html',
            note: '',
            report: null,
            setModel: function(report) {
                    $scope.addNotePopover.note = report.note;
                    $scope.addNotePopover.report = report;
                },
            save: function() {
                $http.post("/service/set-note", {date: $scope.addNotePopover.report.date, note: $scope.addNotePopover.note}).then(
                    function(response) {
                        $scope.addNotePopover.report.note = $scope.addNotePopover.note;
                        document.getElementsByTagName("body")[0].click();
                    }
                );
            }
          };

        $scope.openTicket = function(toDate) {
            if (toDate == undefined) {
                toDate = $scope.ticketToDay.date;
            } else {
                toDate = new Date(toDate);
            }

            var openTicketModal = $uibModal.open({
                animation: true,
                templateUrl: 'open-ticket-template.html',
                controller: 'OpenTicketController',
                resolve: {
                    fields: function () {
                      return {date: toDate, templates: $scope.ticketTemplates};
                    }
                  }
            });

            openTicketModal.result.then(function (response) {
                $scope.linkAtTicket(response.data);
            });
        }

        $scope.linkAtTicket = function(ticketAndLinks) {
            var openTicketModal = $uibModal.open({
                animation: true,
                templateUrl: 'ticket-links-template.html',
                controller: 'SelectTicketLinks',
                resolve: {
                    fields: function () {
                      return {ticketNo: ticketAndLinks.ticketNo, usersLink: ticketAndLinks.usersLink};
                    }
                  }
            });

            openTicketModal.result.then(function (response) {
                if (response.data.success) {
                    swal("Sucesso", "Usuários vinculados ao chamado " + ticketAndLinks.ticketNo + " com sucesso.", "success");
                } else {
                    swal("Erro", "Algum erro aconteceu e os usuários não foram vinculados ao chamado " +
                            ticketAndLinks.ticketNo + ", faça manualmente através da intranet.", "warning");
                }
            });
        }
        // ------------------------------------------------------Tabela de horas


        $scope.$on('login.forbidden', function (event, value) {
            $scope.logout();
        });

        resetFields();
        $interval(updateTodayTime, 30000);
        $scope.tryLogin();
    }]);

    app.controller('DownloadMonthsController', ['$scope', "$http", "$uibModalInstance", function($scope, $http, $uibModalInstance) {
        $scope.number = 1;

        $scope.downMonths = function() {
            $http.get("/service/download-months/"+$scope.number, $scope.credentials)
            .then(function(response) {
                $uibModalInstance.close(response);
            }, function(response) {
                $uibModalInstance.dismiss();
           });
        }
    }]);

    app.controller('UpdateInitialBalanceController',
            ['$scope', "$http", "$uibModalInstance", "fields", function($scope, $http, $uibModalInstance, fields) {
        $scope.initialDate = fields.initialDay;
        $scope.initialBalance = fields.initialBalance;

        $scope.updateInitialBalance = function() {
            var data = {initialDate: $scope.initialDate, initialBalance: $scope.initialBalance};
            $http.post("/service/initial-balance", data).then(
                function(response) {
                    $uibModalInstance.close(response);
                }, function() {
                    $uibModalInstance.dismiss();
                }
            );
        }
    }]);

    app.controller('OpenTicketController',
            ['$scope', "$http", "$uibModalInstance", "fields", function($scope, $http, $uibModalInstance, fields) {
        $scope.date = fields.date;
        $scope.templates = fields.templates;

        $scope.template = $scope.templates[0];
        $scope.dto = {description: "", details: "", priority: 0, expectTime: 0};

        function replaceDate(text) {
            dateFormat =
                    padTime($scope.date.getDate())  + "/" + padTime(($scope.date.getMonth()+1)) + "/" + $scope.date.getFullYear();
            return text.replace("{date}", dateFormat);
        }

        $scope.setTemplate = function() {
            $scope.dto.type = $scope.template.type;
            $scope.dto.description = replaceDate($scope.template.description);
            $scope.dto.details = replaceDate($scope.template.details);
            $scope.dto.priority = $scope.template.priority + "";
            $scope.dto.expectTime = $scope.template.expectTime + "";
        }

        $scope.openTicket = function() {
            $http.post("/service/open-ticket", $scope.dto).then(
                function(response) {
                    $uibModalInstance.close(response);
                }, function() {
                    $uibModalInstance.dismiss();
                }
            );
        }

        $scope.setTemplate();
    }]);

    app.controller('SelectTicketLinks',
            ['$scope', "$http", "$uibModalInstance", "fields", function($scope, $http, $uibModalInstance, fields) {
        $scope.usersLink = fields.usersLink;
        $scope.ticket = {usersLink: [], ticketNo: fields.ticketNo};

        $scope.linkAtTicket = function() {
            $http.post("/service/link-at-ticket", $scope.ticket).then(
                function(response) {
                    $uibModalInstance.close(response);
                }, function() {
                    $uibModalInstance.dismiss();
                }
            );
        }

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };
    }]);

    app.directive("loader", function ($rootScope) {
        return function ($scope, element, attrs) {
            $scope.$on("loader_show", function () {
                document.getElementsByClassName("navbar-brand")[0].focus();
                return element.removeClass("hidden");
            });
            return $scope.$on("loader_hide", function () {
                return element.addClass("hidden");
            });
        };
    });
})();
