(ns material-ui.macros)

(def material-tags
  '[AppBar AppCanvas AutoComplete Avatar Badge BeforeAfterWrapper Card CardActions CardExpandable CardHeader CardMedia CardText CardTitle Checkbox CircularProgress ClearFix DatePicker DatePickerDialog Dialog DropDownIcon DropDownMenu EnhancedButton FlatButton FloatingActionButton FontIcon GridList GridTile IconButton IconMenu LeftNav LinearProgress List ListDivider ListItem Menu MenuItem Overlay Paper Popover RadioButton RadioButtonGroup RaisedButton RefreshIndicator SelectField SelectableContainerEnhance Slider SvgIcon Snackbar Tab Tabs Table TableBody TableFooter TableHeader TableHeaderColumn TableRow TableRowColumn Toggle ThemeWrapper TimePicker TextField Toolbar ToolbarGroup ToolbarSeparator ToolbarTitle Tooltip])


(defn material-ui-react-import [tname]
  `(def ~tname (reagent.core/adapt-react-class (aget js/MaterialUI ~(name tname)))))

(defmacro export-material-ui-react-classes []
  `(do ~@(map material-ui-react-import material-tags)))


