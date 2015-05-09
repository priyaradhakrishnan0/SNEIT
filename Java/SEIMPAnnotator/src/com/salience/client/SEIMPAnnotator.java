package com.salience.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.salience.shared.Annotation;
import com.salience.shared.Concept;
import com.salience.shared.KBAnnotation;
import com.salience.shared.SeimpTrainingRow;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SEIMPAnnotator implements EntryPoint {

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final ServiceAsync proxyService = GWT
			.create(Service.class);
		
	/**
	 * This is the entry point method.
	 */
	
	public static String user=null;
	
	public void onModuleLoad() {
		final DialogBox loginBox=new DialogBox();
		loginBox.setAnimationEnabled(true);
		loginBox.setGlassEnabled(true);
		loginBox.setTitle("This panel allows you to login to the system.");
		
		final VerticalPanel vp=new VerticalPanel();
		final PasswordTextBox ptb=new PasswordTextBox();
		final Button lbutton=new Button("Go");
		vp.add(ptb);
		vp.add(lbutton);
		loginBox.add(vp);
		
		lbutton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(ptb.getText().startsWith("abracadabra")) {
					user=ptb.getText().substring("abracadabra".length());
					loadModule();
					loginBox.hide();
				}
				else
					Window.alert("Sorry better luck next time :)");
			}	
			
		});		
		
		loginBox.center();		
		
	}
	
	
	public void loadModule() {
		final VerticalPanel rootPanel=new VerticalPanel();
		rootPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rootPanel.setSpacing(5);		
		rootPanel.setStyleName("style");
		rootPanel.setTitle("Main Page to do manual annotation of SEIMP dataset.");
		
		//Annotator Selection Panel
		final HorizontalPanel annotatorPanel=new HorizontalPanel();
		annotatorPanel.setSpacing(5);
		annotatorPanel.setTitle("Select the annotator (by name)");
		final Label annotatorLabel=new Label("Select annotator");
		annotatorLabel.setText("Select the annotator (by name)");
		annotatorPanel.add(annotatorLabel);
		final ListBox annotatorListBox=new ListBox();
		if(user!=null && user.trim().length()!=0) annotatorListBox.addItem(user);
		annotatorListBox.setTitle("Click here to get list of available annotators.");
		//annotatorListBox.setVisible(false);
		for(final String name:ClientGlobals.ANNOTATOR_LIST)
			annotatorListBox.addItem(name);
		annotatorPanel.add(annotatorListBox);
		rootPanel.add(annotatorPanel);
		
		//Collection selection panel
		final HorizontalPanel collPanel=new HorizontalPanel();
		collPanel.setSpacing(5);
		collPanel.setTitle("Select the collection (by name)");
		final Label collLabel=new Label("Select collection");
		collLabel.setText("Select the collection (by name)");
		collPanel.add(collLabel);
		final ListBox collListBox=new ListBox();
		collListBox.setTitle("Click here to get list of available collections.");		
		for(final String db:ClientGlobals.COLLECTION_LIST)
			collListBox.addItem(db);
		collPanel.add(collListBox);
		rootPanel.add(collPanel);
		
		//TweetId
		final HorizontalPanel tweetPanel=new HorizontalPanel();
		tweetPanel.setTitle("Tweet id of Total");
		tweetPanel.setSpacing(5);
		final Label tweetPrefixLabel=new Label("Tweet");
		tweetPanel.add(tweetPrefixLabel);
		final TextBox tweetIdTB=new TextBox();
		tweetIdTB.setText("0");
		tweetIdTB.setWidth("60px");
		tweetPanel.add(tweetIdTB);
		final Label tweetSuffixLabel=new Label("of 0");
		tweetPanel.add(tweetSuffixLabel);
		//get the tweet count for the database
		proxyService.getTweetCount(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),new BusyShowAsyncCallback<Integer>(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(Integer result) {
				if(result!=0) { 
					tweetIdTB.setText("1");
					tweetSuffixLabel.setText("of "+result);
				}
			}					
				
		}));
		final Button updateButton=new Button("update");
		updateButton.setTitle("Change to other tweet.");
		tweetPanel.add(updateButton);		
		rootPanel.add(tweetPanel);
		
		//Imp. button
		final HorizontalPanel buttonPanel=new HorizontalPanel();
		buttonPanel.setSpacing(5);
		final Button clearButton=new Button("Clear");
		clearButton.setTitle("Clear the manual annotation for the current tweet.");
		buttonPanel.add(clearButton);
		final Button submitButton=new Button("Submit");
		submitButton.setTitle("Save the manual annotation for the current tweet in db.");
		buttonPanel.add(submitButton);
		final Button prevButton=new Button("Prev");
		prevButton.setTitle("Go to the previous tweet, if possible");
		buttonPanel.add(prevButton);
		final Button nextButton=new Button("Next");
		nextButton.setTitle("Go to the next tweet, if possible");
		buttonPanel.add(nextButton);
		final Button checkButton=new Button("Check");
		checkButton.setTitle("Check un-annotated tweet indices.");
		buttonPanel.add(checkButton);
		final Button xButton=new Button("D");
		checkButton.setTitle("Mark it useless");
		buttonPanel.add(xButton);
		final Button pButton=new Button("P");
		checkButton.setTitle("Mark it pointless");
		buttonPanel.add(pButton);
		final Button sButton=new Button("S");
		checkButton.setTitle("Mark it sarcastic");
		buttonPanel.add(sButton);
		rootPanel.add(buttonPanel);
		
		//Tweet content
		final VerticalPanel contentPanel=new VerticalPanel();
		contentPanel.setTitle("Main panel to select the annotations");
		contentPanel.setSpacing(5);
		contentPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		final Label tweetTextLabel=new Label("<tweet text>");
		tweetTextLabel.setTitle("Tweet text");
		contentPanel.add(tweetTextLabel);
		final HorizontalPanel imagePanel=new HorizontalPanel();
		imagePanel.setTitle("Set of images in the tweet.");
		contentPanel.add(imagePanel);
		final Label instLabel=new Label("Select only the NE's you see in the picture. Select also its Knowledge Base entry.");
		instLabel.setTitle("Annotation instruction");
		contentPanel.add(instLabel);
		final HorizontalPanel neMasterPanel=new HorizontalPanel();
		neMasterPanel.setTitle("List of potential NE's for the tweet displayed.");
		neMasterPanel.setSpacing(15);
		final VerticalPanel nePanel=new VerticalPanel();
		neMasterPanel.add(nePanel);
		final VerticalPanel kbPanel=new VerticalPanel();
		neMasterPanel.add(kbPanel);
		contentPanel.add(neMasterPanel);
		
		final HorizontalPanel commentsPanel=new HorizontalPanel();
		commentsPanel.setSpacing(5);
		final Label commentLabel=new Label("Comments");
		commentsPanel.add(commentLabel);
		final TextBox commentTB=new TextBox();
		commentTB.setWidth("150px");
		commentsPanel.add(commentTB);
		contentPanel.add(commentsPanel);
		final HorizontalPanel aPanel=new HorizontalPanel();
		contentPanel.add(aPanel);
		rootPanel.add(contentPanel);		
				
		final RowAbstract curRow=new RowAbstract();
		
		collListBox.addChangeHandler(new ChangeHandler() {			
			@Override
			public void onChange(ChangeEvent event) {
				proxyService.getTweetCount(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),new BusyShowAsyncCallback<Integer>(new AsyncCallback<Integer>() {
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Integer result) {
						if(result!=0) { 
							tweetIdTB.setText("1");
							tweetSuffixLabel.setText("of "+result);
						}
					}					
						
				}));
				
			}
		});

		updateButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				kbPanel.clear();
				
				//Check the entered tweet index is valid.
				final String tweetId=tweetIdTB.getText();
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);
				if(!tweetId.matches("[0-9]+")) {
					Window.alert("Tweet index contains some non numerals.");
					return;
				}
				final int tIndex=Integer.parseInt(tweetId);				
				if(!(1<=tIndex && tIndex<=maxIndex)) {
					Window.alert("Invalid tweet range.");
					return;
				}
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),tIndex,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(final SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText()+"#"+result.get_id());
						
						//Set the tweet images
						imagePanel.clear();
						if(result.getImageList()!=null && result.getImageList().size()>0) {
							for(final String imageUrl:result.getImageList()){
								final Image image=new Image(imageUrl);
								image.setSize("400px","250px");
								imagePanel.add(image);							
							}
						}
						
						//Set the NE's
						nePanel.clear();
						final String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
						Annotation annotation=isUnAnnotated(result,annotator);
						if(result.getMergedNeList()==null) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=(annotation==null)?(new ArrayList<String>()):annotation.getSneList();
							for(final String ne:result.getMergedNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
								
								neCB.addClickHandler(new ClickHandler() {									
									@Override
									public void onClick(ClickEvent event) {
										kbPanel.clear();
										if(neCB.getValue()==false)	return;
										final List<String> kbEntries=getKbEntry(result, ne);
										if(kbEntries==null) {
											kbPanel.add(new HTML("<font face='verdana' color='red'><b><i>No Kb Entries found</i></b></font>"));
										} else {
											final String kb=selectedKB(curRow.getRow(),ne,annotator);
											for(final String kbEntry:kbEntries){
												final RadioButton button=new RadioButton("kbgroup",kbEntry);
												button.setValue(kbEntry.equalsIgnoreCase(kb));
												kbPanel.add(button);
												
												button.addClickHandler(new ClickHandler() {
													@Override
													public void onClick(ClickEvent event) {
														final SeimpTrainingRow row=curRow.getRow();
														final KBAnnotation ann=new KBAnnotation();
														ann.setKbEntry(button.getText());
														ann.setNe(ne);
														row.setAnnotationList(updateKBAnnotation(row.getAnnotationList(),ann,annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())));
														curRow.setRow(row);
													}
												});
												
											}
										}
									}
								});
								
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(tIndex);
						tweetIdTB.setText(""+tIndex);
						aPanel.clear();
						
						//Set the annotator
						if(result.getAnnotationList()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotationList().size()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						String comments="";
						if(result.getAnnotationList()!=null) {
							for(final Annotation ann:result.getAnnotationList())
								if(ann.getAnnotator().equals(annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())))
									comments=ann.getComments();
						}
						commentTB.setText(comments);
						
					}					
						
				}));
				
			}
		});
		
		clearButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final SeimpTrainingRow row=curRow.getRow();
				if(row==null) {
					Window.alert("Current tweet is null.");
					return;
				}
				kbPanel.clear();
				
				//Clear the annotations
				String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
				if(row.getAnnotationList()!=null) {
					int index=-1;
					for(int i=0;i<row.getAnnotationList().size();i++)
						if(row.getAnnotationList().get(i).getAnnotator().equals(annotator))
								index=i;
					if(index!=-1)
						row.getAnnotationList().remove(index);
				}
				
				proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Void result) {
						Window.alert("Cleared this manual annotation.");
					}					
						
				}));
				
				//Clear the annotations
				if(nePanel.getWidgetCount()>0 && !(nePanel.getWidget(0) instanceof Label)) {
					for(int i=0;i<nePanel.getWidgetCount();i++){
						final CheckBox cbox=(CheckBox)nePanel.getWidget(i);
						if(cbox.getValue())
							cbox.setValue(false);
					}
				}
				
				aPanel.clear();
				aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by "+(row.getAnnotationList()==null?0:row.getAnnotationList().size())+"</i></b></font>"));
				
				//Clear the comments
				commentTB.setText("");
				
			}
			
		});
		
		submitButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final SeimpTrainingRow row=curRow.getRow();
				if(row==null) {
					Window.alert("Current tweet is null.");
					return;
				}
				kbPanel.clear();
				
				//Save the annotator name
				String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
				int annotatedIndex=annotatedIndex(row, annotator);
				Annotation newAnnotation=new Annotation();
				newAnnotation.setAnnotator(annotator);
				newAnnotation.setComments(commentTB.getText());
				newAnnotation.setSneList(null);
				
				//Save the kb annotations
				//newAnnotation=saveKbAnnotation(newAnnotation,row,annotator);
				newAnnotation.setKbList(getKBList(annotator,row));
				
				//Save the sne annotations
				if(nePanel.getWidgetCount()>0 && !(nePanel.getWidget(0) instanceof Label)) {
					final List<String> sneList=new ArrayList<String>();
					for(int i=0;i<nePanel.getWidgetCount();i++){
						final CheckBox cbox=(CheckBox)nePanel.getWidget(i);
						if(cbox.getValue())
							sneList.add(cbox.getText());
					}
					if(sneList.size()>0) newAnnotation.setSneList(sneList);					
				}
				
				if(annotatedIndex==-1) row.addAnnotation(newAnnotation);
				else row.setAnnotation(newAnnotation,annotatedIndex);
				
				proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Void result) {
						//Window.alert("Submitted successfully.");						
					}					
						
				}));
				
				aPanel.clear();
				aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+(row.getAnnotationList()==null?0:row.getAnnotationList().size())+"</i></b></font>"));
				
			}

			private Annotation saveKbAnnotation(Annotation newAnnotation,
					SeimpTrainingRow row, String annotator) {
				if(row==null || row.getAnnotationList()==null || newAnnotation==null || annotator==null || annotator.trim().length()==0) return newAnnotation;
				for(final Annotation ann:row.getAnnotationList()){
					if(ann.getAnnotator().equalsIgnoreCase(annotator)){
						newAnnotation.setKbList(ann.getKbList());
					}
				}
				return newAnnotation;
			}
			
		});
		
		prevButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final int prev=curRow.getCur()-1;
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);				
				if(!(1<=prev && prev<=maxIndex)) {
					Window.alert("Cannot go to previous tweet.");
					return;
				}			
				kbPanel.clear();
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),prev,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(final SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText()+"#"+result.get_id());
						
						//Set the tweet images
						imagePanel.clear();
						if(result.getImageList()!=null && result.getImageList().size()>0) {
							for(final String imageUrl:result.getImageList()){
								final Image image=new Image(imageUrl);
								image.setSize("400px","250px");
								imagePanel.add(image);							
							}
						}
						
						//Set the NE's
						nePanel.clear();
						final String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
						Annotation annotation=isUnAnnotated(result,annotator);
						if(result.getMergedNeList()==null) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=(annotation==null)?(new ArrayList<String>()):annotation.getSneList();
							for(final String ne:result.getMergedNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
								
								neCB.addClickHandler(new ClickHandler() {									
									@Override
									public void onClick(ClickEvent event) {
										kbPanel.clear();
										if(neCB.getValue()==false)	return;
										final List<String> kbEntries=getKbEntry(result, ne);
										if(kbEntries==null) {
											kbPanel.add(new HTML("<font face='verdana' color='red'><b><i>No Kb Entries found</i></b></font>"));
										} else {
											final String kb=selectedKB(curRow.getRow(),ne,annotator);
											for(final String kbEntry:kbEntries){
												final RadioButton button=new RadioButton("kbgroup",kbEntry);
												button.setValue(kbEntry.equalsIgnoreCase(kb));
												kbPanel.add(button);
												
												button.addClickHandler(new ClickHandler() {
													@Override
													public void onClick(ClickEvent event) {
														final SeimpTrainingRow row=curRow.getRow();
														final KBAnnotation ann=new KBAnnotation();
														ann.setKbEntry(button.getText());
														ann.setNe(ne);
														row.setAnnotationList(updateKBAnnotation(row.getAnnotationList(),ann,annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())));
														curRow.setRow(row);
													}
												});
												
											}
										}
									}
								});
								
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(prev);
						tweetIdTB.setText(""+prev);
						aPanel.clear();
						
						//Set the annotator
						if(result.getAnnotationList()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotationList().size()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						String comments="";
						if(result.getAnnotationList()!=null) {
							for(final Annotation ann:result.getAnnotationList())
								if(ann.getAnnotator().equals(annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())))
									comments=ann.getComments();
						}
						commentTB.setText(comments);
					}					
						
				}));
				
			}
			
		});
		
		nextButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final int next=curRow.getCur()+1;
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);				
				if(!(1<=next && next<=maxIndex)) {
					Window.alert("Cannot go to next tweet.");
					return;
				}				
				kbPanel.clear();
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),next,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(final SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText()+"#"+result.get_id());
						
						//Set the tweet images
						imagePanel.clear();
						if(result.getImageList()!=null && result.getImageList().size()>0) {
							for(final String imageUrl:result.getImageList()){
								final Image image=new Image(imageUrl);
								image.setSize("400px","250px");
								imagePanel.add(image);							
							}
						}
						
						//Set the NE's
						nePanel.clear();
						final String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
						Annotation annotation=isUnAnnotated(result,annotator);
						if(result.getMergedNeList()==null) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=(annotation==null)?(new ArrayList<String>()):annotation.getSneList();
							for(final String ne:result.getMergedNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
								
								neCB.addClickHandler(new ClickHandler() {									
									@Override
									public void onClick(ClickEvent event) {
										kbPanel.clear();
										if(neCB.getValue()==false)	return;
										final List<String> kbEntries=getKbEntry(result, ne);
										if(kbEntries==null) {
											kbPanel.add(new HTML("<font face='verdana' color='red'><b><i>No Kb Entries found</i></b></font>"));
										} else {
											final String kb=selectedKB(curRow.getRow(),ne,annotator);
											for(final String kbEntry:kbEntries){
												final RadioButton button=new RadioButton("kbgroup",kbEntry);
												button.setValue(kbEntry.equalsIgnoreCase(kb));
												kbPanel.add(button);
												
												button.addClickHandler(new ClickHandler() {
													@Override
													public void onClick(ClickEvent event) {
														final SeimpTrainingRow row=curRow.getRow();
														final KBAnnotation ann=new KBAnnotation();
														ann.setKbEntry(button.getText());
														ann.setNe(ne);
														row.setAnnotationList(updateKBAnnotation(row.getAnnotationList(),ann,annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())));
														curRow.setRow(row);
													}
												});
												
											}
										}
									}
								});
								
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(next);
						tweetIdTB.setText(""+next);
						aPanel.clear();
						
						//Set the annotator
						if(result.getAnnotationList()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotationList().size()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						String comments="";
						if(result.getAnnotationList()!=null) {
							for(final Annotation ann:result.getAnnotationList())
								if(ann.getAnnotator().equals(annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())))
									comments=ann.getComments();
						}
						commentTB.setText(comments);
						
					}					
						
				}));
				
			}
			
		});
		
		checkButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				proxyService.getUnannotatedList(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),annotatorListBox.getItemText(annotatorListBox.getSelectedIndex()),new BusyShowAsyncCallback<List<String>>(new AsyncCallback<List<String>>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(List<String> result) {
						if(result==null || result.size()==0) {
							Window.alert("Nothing left to annotate. Enjoy!");
							return;
						}
						final StringBuffer buff=new StringBuffer();
						for(final String un:result)
							buff.append(un+",");
						Window.alert(buff.toString());
					}					
						
				}));
				
			}
			
		});
		
		xButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final SeimpTrainingRow row=curRow.getRow();
				if(row==null) {
					Window.alert("Current tweet is null.");
					return;
				}
				kbPanel.clear();
				
				//Save the annotator name
				String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
				int annotatedIndex=annotatedIndex(row, annotator);
				Annotation newAnnotation=new Annotation();
				newAnnotation.setAnnotator(annotator);
				newAnnotation.setComments("D");
				newAnnotation.setSneList(null);
				if(annotatedIndex==-1) {
					row.addAnnotation(newAnnotation);
				} else {
					row.setAnnotation(newAnnotation,annotatedIndex);
				}
				
				proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Void result) {
						//Window.alert("Submitted successfully.");						
					}					
						
				}));
				
				//Go to next tweet.
				
				final int next=curRow.getCur()+1;
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);				
				if(!(1<=next && next<=maxIndex)) {
					Window.alert("Cannot go to next tweet.");
					return;
				}				
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),next,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(final SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText()+"#"+result.get_id());
						
						//Set the tweet images
						imagePanel.clear();
						if(result.getImageList()!=null && result.getImageList().size()>0) {
							for(final String imageUrl:result.getImageList()){
								final Image image=new Image(imageUrl);
								image.setSize("400px","250px");
								imagePanel.add(image);							
							}
						}
						
						//Set the NE's
						nePanel.clear();
						final String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
						Annotation annotation=isUnAnnotated(result,annotator);
						if(result.getMergedNeList()==null) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=(annotation==null)?(new ArrayList<String>()):annotation.getSneList();
							for(final String ne:result.getMergedNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
								
								neCB.addClickHandler(new ClickHandler() {									
									@Override
									public void onClick(ClickEvent event) {
										kbPanel.clear();
										if(neCB.getValue()==false)	return;
										final List<String> kbEntries=getKbEntry(result, ne);
										if(kbEntries==null) {
											kbPanel.add(new HTML("<font face='verdana' color='red'><b><i>No Kb Entries found</i></b></font>"));
										} else {
											final String kb=selectedKB(curRow.getRow(),ne,annotator);
											for(final String kbEntry:kbEntries){
												final RadioButton button=new RadioButton("kbgroup",kbEntry);
												button.setValue(kbEntry.equalsIgnoreCase(kb));
												kbPanel.add(button);
												
												button.addClickHandler(new ClickHandler() {
													@Override
													public void onClick(ClickEvent event) {
														final SeimpTrainingRow row=curRow.getRow();
														final KBAnnotation ann=new KBAnnotation();
														ann.setKbEntry(button.getText());
														ann.setNe(ne);
														row.setAnnotationList(updateKBAnnotation(row.getAnnotationList(),ann,annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())));
														curRow.setRow(row);
													}
												});
												
											}
										}
									}
								});
								
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(next);
						tweetIdTB.setText(""+next);
						aPanel.clear();
						
						//Set the annotator
						if(result.getAnnotationList()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotationList().size()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						String comments="";
						if(result.getAnnotationList()!=null) {
							for(final Annotation ann:result.getAnnotationList())
								if(ann.getAnnotator().equals(annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())))
									comments=ann.getComments();
						}
						commentTB.setText(comments);
						
					}					
						
				}));
				

				
			}
		});
		
		pButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final SeimpTrainingRow row=curRow.getRow();
				if(row==null) {
					Window.alert("Current tweet is null.");
					return;
				}
				kbPanel.clear();
				
				//Save the annotator name
				String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
				int annotatedIndex=annotatedIndex(row, annotator);
				Annotation newAnnotation=new Annotation();
				newAnnotation.setAnnotator(annotator);
				newAnnotation.setComments("P");
				newAnnotation.setSneList(null);
				if(annotatedIndex==-1) {
					row.addAnnotation(newAnnotation);
				} else {
					row.setAnnotation(newAnnotation,annotatedIndex);
				}
				
				proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Void result) {
						//Window.alert("Submitted successfully.");						
					}					
						
				}));
				
				//Go to next tweet.
				
				final int next=curRow.getCur()+1;
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);				
				if(!(1<=next && next<=maxIndex)) {
					Window.alert("Cannot go to next tweet.");
					return;
				}				
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),next,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(final SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText()+"#"+result.get_id());
						
						//Set the tweet images
						imagePanel.clear();
						if(result.getImageList()!=null && result.getImageList().size()>0) {
							for(final String imageUrl:result.getImageList()){
								final Image image=new Image(imageUrl);
								image.setSize("400px","250px");
								imagePanel.add(image);							
							}
						}
						
						//Set the NE's
						nePanel.clear();
						final String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
						Annotation annotation=isUnAnnotated(result,annotator);
						if(result.getMergedNeList()==null) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=(annotation==null)?(new ArrayList<String>()):annotation.getSneList();
							for(final String ne:result.getMergedNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
								
								neCB.addClickHandler(new ClickHandler() {									
									@Override
									public void onClick(ClickEvent event) {
										kbPanel.clear();
										if(neCB.getValue()==false)	return;
										final List<String> kbEntries=getKbEntry(result, ne);
										if(kbEntries==null) {
											kbPanel.add(new HTML("<font face='verdana' color='red'><b><i>No Kb Entries found</i></b></font>"));
										} else {
											final String kb=selectedKB(curRow.getRow(),ne,annotator);
											for(final String kbEntry:kbEntries){
												final RadioButton button=new RadioButton("kbgroup",kbEntry);
												button.setValue(kbEntry.equalsIgnoreCase(kb));
												kbPanel.add(button);
												
												button.addClickHandler(new ClickHandler() {
													@Override
													public void onClick(ClickEvent event) {
														final SeimpTrainingRow row=curRow.getRow();
														final KBAnnotation ann=new KBAnnotation();
														ann.setKbEntry(button.getText());
														ann.setNe(ne);
														row.setAnnotationList(updateKBAnnotation(row.getAnnotationList(),ann,annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())));
														curRow.setRow(row);
													}
												});
												
											}
										}
									}
								});
								
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(next);
						tweetIdTB.setText(""+next);
						aPanel.clear();
						
						//Set the annotator
						if(result.getAnnotationList()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotationList().size()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						String comments="";
						if(result.getAnnotationList()!=null) {
							for(final Annotation ann:result.getAnnotationList())
								if(ann.getAnnotator().equals(annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())))
									comments=ann.getComments();
						}
						commentTB.setText(comments);
						
					}					
						
				}));
				

				
			}
		});
		
		sButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final SeimpTrainingRow row=curRow.getRow();
				if(row==null) {
					Window.alert("Current tweet is null.");
					return;
				}
				kbPanel.clear();
				
				//Save the annotator name
				String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
				int annotatedIndex=annotatedIndex(row, annotator);
				Annotation newAnnotation=new Annotation();
				newAnnotation.setAnnotator(annotator);
				newAnnotation.setComments("S");
				newAnnotation.setSneList(null);
				if(annotatedIndex==-1) {
					row.addAnnotation(newAnnotation);
				} else {
					row.setAnnotation(newAnnotation,annotatedIndex);
				}
				
				proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Void result) {
						//Window.alert("Submitted successfully.");						
					}					
						
				}));
				
				//Go to next tweet.
				
				final int next=curRow.getCur()+1;
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);				
				if(!(1<=next && next<=maxIndex)) {
					Window.alert("Cannot go to next tweet.");
					return;
				}				
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),next,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(final SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText()+"#"+result.get_id());
						
						//Set the tweet images
						imagePanel.clear();
						if(result.getImageList()!=null && result.getImageList().size()>0) {
							for(final String imageUrl:result.getImageList()){
								final Image image=new Image(imageUrl);
								image.setSize("400px","250px");
								imagePanel.add(image);							
							}
						}
						
						//Set the NE's
						nePanel.clear();
						final String annotator=annotatorListBox.getItemText(annotatorListBox.getSelectedIndex());
						Annotation annotation=isUnAnnotated(result,annotator);
						if(result.getMergedNeList()==null) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=(annotation==null)?(new ArrayList<String>()):annotation.getSneList();
							for(final String ne:result.getMergedNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
								
								neCB.addClickHandler(new ClickHandler() {									
									@Override
									public void onClick(ClickEvent event) {
										kbPanel.clear();
										if(neCB.getValue()==false)	return;
										final List<String> kbEntries=getKbEntry(result, ne);
										if(kbEntries==null) {
											kbPanel.add(new HTML("<font face='verdana' color='red'><b><i>No Kb Entries found</i></b></font>"));
										} else {
											final String kb=selectedKB(curRow.getRow(),ne,annotator);
											for(final String kbEntry:kbEntries){
												final RadioButton button=new RadioButton("kbgroup",kbEntry);
												button.setValue(kbEntry.equalsIgnoreCase(kb));
												kbPanel.add(button);
												
												button.addClickHandler(new ClickHandler() {
													@Override
													public void onClick(ClickEvent event) {
														final SeimpTrainingRow row=curRow.getRow();
														final KBAnnotation ann=new KBAnnotation();
														ann.setKbEntry(button.getText());
														ann.setNe(ne);
														row.setAnnotationList(updateKBAnnotation(row.getAnnotationList(),ann,annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())));
														curRow.setRow(row);
													}
												});
												
											}
										}
									}
								});
								
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(next);
						tweetIdTB.setText(""+next);
						aPanel.clear();
						
						//Set the annotator
						if(result.getAnnotationList()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotationList().size()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						String comments="";
						if(result.getAnnotationList()!=null) {
							for(final Annotation ann:result.getAnnotationList())
								if(ann.getAnnotator().equals(annotatorListBox.getItemText(annotatorListBox.getSelectedIndex())))
									comments=ann.getComments();
						}
						commentTB.setText(comments);
						
					}					
						
				}));
				

				
			}
		});
		
		
		RootPanel.get().add(rootPanel);
		
		
		/*
		proxyService.getTweetCount(ClientGlobals.MONGO_DB_NAME,"smallseimptrainingset",new BusyShowAsyncCallback<Integer>(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(Integer result) {
				Window.alert(""+result);				
			}					
				
		}));
		
		proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,"smallseimptrainingset",1,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(SeimpTrainingRow result) {
				Window.alert(result.get_id()+"\t"+result.getText()+"\t"+result.getCreatedAt());				
			}					
				
		}));
		
		SeimpTrainingRow row=new SeimpTrainingRow();
		row.set_id(1);
		row.setSneList(Arrays.asList("1","2"));
		row.setAnnotator("g");

		proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,"test",row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(Void result) {
				Window.alert("hello");
			}					
				
		}));
		
		proxyService.getUnannotatedList(ClientGlobals.MONGO_DB_NAME,"smallseimptrainingset",new BusyShowAsyncCallback<List<String>>(new AsyncCallback<List<String>>(){
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(List<String> result) {
				String str="I-";
				for(String res:result)
					str+=res+"\t";
				Window.alert(str);
			}					
				
		}));
		*/
		
		
		
		
	}
	
	private Annotation isUnAnnotated(final SeimpTrainingRow row,final String user){
		//Returns annotation if the row is unannotated by any user or by this user.
		if(row.getAnnotationList()==null || row.getAnnotationList().size()==0) return null;
		for(final Annotation ann:row.getAnnotationList())
			if(ann.getAnnotator().equals(user))
				return ann;
		return null;
	}
	
	private int annotatedIndex(final SeimpTrainingRow row,final String user){
		//Returns annotation index if the row is by this user.
		if(row.getAnnotationList()==null || row.getAnnotationList().size()==0) return -1;
		for(int i=0;i<row.getAnnotationList().size();i++)
			if(row.getAnnotationList().get(i).getAnnotator().equals(user))
				return i;
		return -1;
	}
	
	private List<String> getKbEntry(final SeimpTrainingRow row,String ne){
		if(row==null || row.getConceptList()==null || ne==null || ne.length()==0) return null;
		System.out.println(row.get_id());
		ne=ne.substring(0,ne.lastIndexOf("(")-1).trim();
		for(final Concept concept:row.getConceptList()){
			if(concept.getNe().equalsIgnoreCase(ne)) {
				return pickFirstNEntries(concept.getKbEntries(),ClientGlobals.MAX_KB_ENTRIES);
			}
		}
		return null;
	}
	
	private List<String> pickFirstNEntries(final List<String> list,int N){
		if(list==null || list.size()==0 || N==0) return null;
		final List<String> resList=new ArrayList<String>();
		for(int i=0;(i<N && i<list.size());i++)
			resList.add(list.get(i));
		resList.add("none");
		return resList;
	}
	
	private List<Annotation> updateKBAnnotation(List<Annotation> annList,final KBAnnotation newAnn,final String user){
		if(annList==null || annList.size()==0 || newAnn==null) annList=new ArrayList<Annotation>();
		boolean isUserFound=false;
		for(final Annotation ann:annList) {
			if(ann.getAnnotator().equalsIgnoreCase(user)) {
				List<KBAnnotation> kbList=ann.getKbList();
				if(kbList==null){
					kbList=new ArrayList<KBAnnotation>();
					kbList.add(newAnn);
					ann.setKbList(kbList);
				} else {
					boolean isFound=false;
					for(final KBAnnotation a:kbList){
						if(a.getNe().equalsIgnoreCase(newAnn.getNe())) {
							a.setKbEntry(newAnn.getKbEntry());
							isFound=true;
						}
					}
					if(!isFound) kbList.add(newAnn);
				}
				isUserFound=true;
			}
		}
		if(!isUserFound){
			final Annotation ann=new Annotation();
			ann.setAnnotator(user);
			ann.addKbEntry(newAnn);
			annList.add(ann);
		}		
		return annList;
	}
	
	private String selectedKB(final SeimpTrainingRow row,final String ne,final String annotator){
		if(row==null || row.getAnnotationList()==null || ne==null || ne.trim().length()==0 || annotator==null || annotator.trim().length()==0)
			return null;
		for(final Annotation ann:row.getAnnotationList()){
			if(ann.getKbList()!=null) {
				for(final KBAnnotation kbAnn:ann.getKbList())
					if(ann.getAnnotator().equalsIgnoreCase(annotator) && kbAnn.getNe().equals(ne))
						return kbAnn.getKbEntry();
			}
		}
		return null;
	}
	
	private List<KBAnnotation> getKBList(final String user,final SeimpTrainingRow row){
		if(row.getAnnotationList()==null || row.getAnnotationList().size()==0) return null;
		for(final Annotation ann:row.getAnnotationList()){
			if(ann.getAnnotator().equalsIgnoreCase(user))
				return ann.getKbList();
		}
		return null;
	}
	
}

class RowAbstract{
	SeimpTrainingRow row=null;
	int cur=0;

	public int getCur() {
		return cur;
	}

	public void setCur(int cur) {
		this.cur = cur;
	}

	public SeimpTrainingRow getRow() {
		return row;
	}

	public void setRow(SeimpTrainingRow row) {
		this.row = row;
	}
	
}
